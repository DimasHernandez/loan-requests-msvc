package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.common.PageResponse;
import co.com.pragma.model.exceptions.*;
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

    private static final String DEFAULT_STATUS_LOAN = "PENDING_REVIEW";
    private static final List<String> FINAL_LOAN_STATUSES = List.of("APPROVED", "REJECTED");

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

                                if (loanApplication.getLoanType().isAutomaticValidation()) {
                                    System.out.println("Simulando envio de mensaje a SQS de AWS"); // TODO: delete this
                                    return loanValidationUseCase.enqueueLoanValidation(loanApp, user)
                                            .thenReturn(loanApp);
                                }
                                loanApp.setTotalMonthlyDebtApprovedApplications(savedLoanApp.getTotalMonthlyDebtApprovedApplications());
                                return Mono.just(loanApp);
                            });
                })
                .doOnSuccess(loan ->
                        logger.info("Loan application registered. Status: {}, Document: {}", DEFAULT_STATUS_LOAN,
                                loan.getDocumentNumber()));
    }

    private Mono<Tuple2<LoanApplication, User>> searchUserAndAssignEmail(LoanApplication loanApplication, String email, String token) {
        return userRestConsumer.findUserByEmail(email, token)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario no encontrado")))
                .filter(user -> user.getDocumentNumber().equals(loanApplication.getDocumentNumber()))
                .switchIfEmpty(Mono.error(new AccessDeniedException("No puedes crear préstamos a nombre de otro usuario")))
                .map(user -> {
                            loanApplication.setEmail(user.getEmail());
                            loanApplication.setTotalMonthlyDebtApprovedApplications(BigDecimal.ZERO);
                            return Tuples.of(loanApplication, user);
                        }
                );
    }

    private Mono<LoanApplication> assignLoanType(LoanApplication loanApplication) {
        return loanTypeRepository.findByName(loanApplication.getLoanType().getName())
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException("Tipo de prestamo no encontrado")))
                .flatMap(loanType -> {
                    if (!isAmountValid(loanApplication.getAmount(), loanType)) {
                        return Mono.error(new AmountOutOfRangeException("El monto no es valido"));
                    }
                    if (!isTermValid(loanApplication.getTermMonth(), loanType)) {
                        return Mono.error(new TermOutOfRangeException("El plazo establecido no es valido"));
                    }
                    loanApplication.setLoanType(loanType);
                    return Mono.just(loanApplication);
                });
    }

    private Mono<LoanApplication> assignStatus(LoanApplication loanApplication) {
        return statusRepository.findByName(DEFAULT_STATUS_LOAN)
                .switchIfEmpty(Mono.error(new StatusNotFoundException("Estado del prestamo no encontrado")))
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
                                        "El usuario ya cuenta con una solicitud de préstamo en proceso del mismo tipo"));
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
            return Mono.error(new FinalStateNotAllowedException("Estado no permitido. [APPROVED, REJECTED]"));
        }

        return transactionalWrapper.transactional(
                Mono.zip(
                                loanApplicationRepository.findLoanApplicationById(loanApplicationId)
                                        .switchIfEmpty(Mono.error(new LoanApplicationNotFoundException("Solicitud de préstamo no encontrada"))),
                                statusRepository.findByName(statusToUpdated)
                                        .switchIfEmpty(Mono.error(new StatusNotFoundException("Estado no encontrado")))
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
                                        return Mono.error(new FinalStateNotAllowedException("La solicitud de préstamo ya se " +
                                                "encuentra en estado final " + previousStatus.getName()));
                                    }

                                    loanApp.setStatus(newStatus);
                                    return loanApplicationRepository.saveLoanApplication(loanApp)
                                            .flatMap(updatedLoan -> {
                                                UpdatedLoanApplication response = new UpdatedLoanApplication().toBuilder()
                                                        .id(updatedLoan.getId())
                                                        .email(updatedLoan.getEmail())
                                                        .previousStatus(previousStatus.getName())
                                                        .newStatus(newStatus.getName())
                                                        .message("Loan Status Update Successful")
                                                        .build();

                                                return loanStatusMessageGateway.send(response)
                                                        .doOnSuccess(msgId -> logger.info("Loan status event sent to SQS with message_id= {}", msgId))
                                                        .doOnError(e -> logger.error("Failed to send loan status event to SQS", e))
                                                        .thenReturn(response);

                                            });
                                }
                        )
        );
    }

}
