package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.common.PageResponse;
import co.com.pragma.model.enums.STATES;
import co.com.pragma.model.exceptions.*;
import co.com.pragma.model.exceptions.enums.ErrorMessages;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.UpdatedLoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoanStatusMessageGateway;
import co.com.pragma.model.loanapplication.gateways.LoggerPort;
import co.com.pragma.model.loanapplication.gateways.TransactionalWrapper;
import co.com.pragma.model.loanreviewitem.LoanReviewItem;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRestConsumerPort;
import co.com.pragma.model.userbasicinfo.UserBasicInfo;
import co.com.pragma.usecase.loanvalidation.LoanValidationUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private static final String DEFAULT_STATUS_LOAN = STATES.PENDING_REVIEW.name();
    private static final List<String> FINAL_LOAN_STATUSES = List.of(STATES.APPROVED.name(), STATES.REJECTED.name());
    private static final String PUBLISHING_SUCCESSFUL_MESSAGE = "Loan Status Update Successful";

    private final LoanTypeRepository loanTypeRepository;
    private final StatusRepository statusRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRestConsumerPort userRestConsumer;
    private final TransactionalWrapper transactionalWrapper;
    private final LoanStatusMessageGateway loanStatusMessageGateway;
    private final LoanValidationUseCase loanValidationUseCase;
    private final LoggerPort logger;

    public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication, String email, String token) {
        return searchUserAndAssignEmail(loanApplication, email, token)
                .flatMap(tuple -> {
                    LoanApplication loanApp = tuple.getT1();
                    User user = tuple.getT2();
                    return assignLoanType(loanApp)
                            .flatMap(this::assignStatus)
                            .flatMap(this::validateLoanApplicationStateAndType)
                            .flatMap(loanApplicationRepository::saveLoanApplication)
                            .flatMap(savedLoanApp -> {

                                loanApp.setId(savedLoanApp.getId());

                                if (loanApp.getLoanType().isAutomaticValidation()) {
                                    return loanValidationUseCase.enqueueLoanValidation(loanApp, user)
                                            .thenReturn(loanApp);
                                }
                                return Mono.just(loanApp);
                            });
                })
                .doOnSuccess(loan ->
                        logger.info("Loan application registered. Status: {}, Document: {}", DEFAULT_STATUS_LOAN,
                                loan.getDocumentNumber()));
    }

    private Mono<Tuple2<LoanApplication, User>> searchUserAndAssignEmail(LoanApplication loanApplication, String email, String token) {
        return userRestConsumer.findUserByEmail(email, token)
                .switchIfEmpty(Mono.error(new UserNotFoundException(ErrorMessages.USER_NOT_FOUND.getMessage())))
                .filter(user -> user.getDocumentNumber().equals(loanApplication.getDocumentNumber()))
                .switchIfEmpty(Mono.error(new AccessDeniedException(ErrorMessages.ACCESS_DENIED.getMessage())))
                .map(user -> {
                            loanApplication.setEmail(user.getEmail());
                            return Tuples.of(loanApplication, user);
                        }
                );
    }

    private Mono<LoanApplication> assignLoanType(LoanApplication loanApplication) {
        return loanTypeRepository.findByName(loanApplication.getLoanType().getName())
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException(ErrorMessages.LOAN_TYPE_NOT_FOUND.getMessage())))
                .flatMap(loanType -> {
                    if (!isAmountValid(loanApplication.getAmount(), loanType)) {
                        return Mono.error(new AmountOutOfRangeException(ErrorMessages.AMOUNT_OUT_RANGE.getMessage()));
                    }
                    if (!isTermValid(loanApplication.getTermMonth(), loanType)) {
                        return Mono.error(new TermOutOfRangeException(ErrorMessages.TERM_OUT_RANGE.getMessage()));
                    }
                    loanApplication.setLoanType(loanType);
                    return Mono.just(loanApplication);
                });
    }

    private Mono<LoanApplication> assignStatus(LoanApplication loanApplication) {
        return statusRepository.findByName(DEFAULT_STATUS_LOAN)
                .switchIfEmpty(Mono.error(new StatusNotFoundException(ErrorMessages.STATUS_NOT_FOUND.getMessage())))
                .map(status -> {
                    loanApplication.setStatus(status);
                    return loanApplication;
                });
    }

    private Mono<LoanApplication> validateLoanApplicationStateAndType(LoanApplication loanApp) {
        return loanApplicationRepository.existsUserAndLoanTypeAndStatus(loanApp.getDocumentNumber(),
                        loanApp.getLoanType().getId(), loanApp.getStatus().getId())
                .flatMap(isLoanType -> {
                            if (Boolean.TRUE.equals(isLoanType)) {
                                return Mono.error(new LoanRequestStatusAndTypeMismatchException(
                                        ErrorMessages.LOAN_REQUEST_STATUS_MISMATCH.getMessage()));
                            }
                            return Mono.just(loanApp);
                        }
                );
    }

    private boolean isAmountValid(BigDecimal amount, LoanType loanType) {
        return amount.compareTo(loanType.getAmountMin()) >= 0 &&
                amount.compareTo(loanType.getAmountMax()) <= 0;
    }

    private boolean isTermValid(Integer term, LoanType loanType) {
        return term >= loanType.getTermMonthMin() && term <= loanType.getTermMonthMax();
    }

    public Mono<PageResponse<LoanReviewItem>> getLoanApplicationsForReview(List<String> statuses, int page, int size, String token) {
        int offSet = page * size;
        return loanApplicationRepository.countLoanApplicationByStatusesIn(statuses)
                .flatMap(totalElements ->
                        loanApplicationRepository.findLoanApplicationWithDetails(statuses, size, offSet)
                                .collectList()
                                .flatMap(content -> {
                                    List<String> emails = content.stream().map(LoanReviewItem::getEmail)
                                            .distinct().toList();

                                    if (emails.isEmpty()) {
                                        return Mono.just(new PageResponse<>(content, page, size, totalElements,
                                                (int) Math.ceil(totalElements / (double) size)));
                                    }

                                    return userRestConsumer.findUsersByBatchEmails(emails, token)
                                            .collectMap(UserBasicInfo::getEmail)
                                            .map(usersMap -> {
                                                List<LoanReviewItem> loanUpdatedList = content.stream()
                                                        .map(item -> {
                                                            UserBasicInfo user = usersMap.get(item.getEmail());
                                                            return item.toBuilder()
                                                                    .fullName(user.getName().concat(" ").concat(user.getSurname()))
                                                                    .baseSalary(user.getBaseSalary())
                                                                    .build();
                                                        }).toList();

                                                int totalPages = (int) Math.ceil(totalElements / (double) size);
                                                return new PageResponse<>(loanUpdatedList, page, size, totalElements, totalPages);
                                            })
                                            .doOnSuccess(pageResponse ->
                                                    logger.info("checking the list of loan applications successfully. " +
                                                                    "content_size: {} - page: {} - size: {} - total_elements: {} - total_pages: {} ",
                                                            pageResponse.getContent().size(), page, size, totalElements, pageResponse.getTotalPages())
                                            );
                                })
                );
    }

    public Mono<UpdatedLoanApplication> updatedLoanApplicationStatus(UUID loanApplicationId, String statusToUpdated) {

        if (!FINAL_LOAN_STATUSES.contains(statusToUpdated)) {
            return Mono.error(new FinalStateNotAllowedException(ErrorMessages.FINAL_STATE_NOT_ALLOWED.getMessage()));
        }

        return transactionalWrapper.transactional(
                Mono.zip(
                                loanApplicationRepository.findLoanApplicationById(loanApplicationId)
                                        .switchIfEmpty(Mono.error(new LoanApplicationNotFoundException(ErrorMessages.LOAN_APPLICATION_NOT_FOUND.getMessage()))),
                                statusRepository.findByName(statusToUpdated)
                                        .switchIfEmpty(Mono.error(new StatusNotFoundException(ErrorMessages.STATUS_NOT_FOUND.getMessage())))
                        )
                        .flatMap(tuple -> {
                            LoanApplication loanAppBd = tuple.getT1();
                            Status newStatus = tuple.getT2();

                            return statusRepository.findStatusById(loanAppBd.getStatus().getId())
                                    .map(previousStatus -> Tuples.of(loanAppBd, previousStatus, newStatus));
                        })
                        .flatMap(tuple -> {
                                    LoanApplication loanApp = tuple.getT1();
                                    Status previousStatus = tuple.getT2();
                                    Status newStatus = tuple.getT3();

                                    if (FINAL_LOAN_STATUSES.contains(previousStatus.getName())) {
                                        return Mono.error(new FinalStateNotAllowedException(ErrorMessages.FINAL_STATE_NOT_ALLOWED_CUSTOM.getMessage()
                                                + previousStatus.getName()));
                                    }

                                    loanApp.setStatus(newStatus);
                                    return loanApplicationRepository.saveLoanApplication(loanApp)
                                            .flatMap(updatedLoan -> {
                                                UpdatedLoanApplication response = new UpdatedLoanApplication().toBuilder()
                                                        .id(updatedLoan.getId())
                                                        .email(updatedLoan.getEmail())
                                                        .previousStatus(previousStatus.getName())
                                                        .newStatus(newStatus.getName())
                                                        .message(PUBLISHING_SUCCESSFUL_MESSAGE)
                                                        .build();

                                                return loanStatusMessageGateway.send(response)
                                                        .doOnSuccess(msgId -> logger.info("Loan status event sent to SQS with message_id: {}", msgId))
                                                        .doOnError(e -> logger.error("Failed to send loan status event to SQS", e))
                                                        .thenReturn(response);

                                            });
                                }
                        )
        );
    }

}
