package co.com.pragma.usecase.loanvalidation;

import co.com.pragma.model.enums.STATES;
import co.com.pragma.model.exceptions.LoanApplicationNotFoundException;
import co.com.pragma.model.exceptions.StatusNotFoundException;
import co.com.pragma.model.exceptions.enums.ErrorMessages;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoggerPort;
import co.com.pragma.model.loanvalidation.events.request.LoanValidationRequest;
import co.com.pragma.model.loanvalidation.events.response.LoanValidationResponse;
import co.com.pragma.model.loanvalidation.gateway.LoanValidationGateway;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.model.user.User;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class LoanValidationUseCase {

    private static final List<String> VALID_STATES_UPDATING = List.of(STATES.APPROVED.name(), STATES.REJECTED.name(),
            STATES.MANUAL_REVIEW.name());

    private final LoanValidationGateway loanValidationGateway;
    private final LoanApplicationRepository loanApplicationRepository;
    private final StatusRepository statusRepository;
    private final LoggerPort logger;

    public Mono<LoanApplication> enqueueLoanValidation(LoanApplication loanApp, User user) {
        return loanApplicationRepository.findLoanApplicationByDocumentNumberAndStatus(user.getDocumentNumber(), STATES.APPROVED.name())
                .collectList()
                .flatMap(approvedLoans ->
                        statusRepository.findByName(STATES.PENDING_VALIDATION.name())
                                .switchIfEmpty(Mono.error(new StatusNotFoundException(ErrorMessages.STATUS_NOT_FOUND.getMessage())))
                                .flatMap(pendingValidationStatus -> {
                                    loanApp.setStatus(pendingValidationStatus);
                                    return loanApplicationRepository.saveLoanApplication(loanApp)
                                            .flatMap(savedLoanApp -> {
                                                LoanValidationRequest request = LoanValidationRequest.builder()
                                                        .loanId(savedLoanApp.getId())
                                                        .documentNumber(user.getDocumentNumber())
                                                        .fullName(user.getName().concat(" ").concat(user.getSurname()))
                                                        .amount(loanApp.getAmount())
                                                        .termMonth(loanApp.getTermMonth())
                                                        .interestRate(loanApp.getLoanType().getInterestRate())
                                                        .baseSalary(user.getBaseSalary())
                                                        .activeLoans(approvedLoans)
                                                        .build();
                                                return loanValidationGateway.sendToQueue(request)
                                                        .doOnSuccess(messageId -> logger.info("Sent to SQS in AWS with message_id: {}", messageId))
                                                        .thenReturn(loanApp);
                                            });
                                })
                );
    }

    public Mono<Void> processMessageResultQueue(LoanValidationResponse loanValidationResponse) {
        String statusFinal = loanValidationResponse.getResult();
        UUID loanId = loanValidationResponse.getLoanId();

        if (shouldSkipProcessing(statusFinal, loanId)) {
            return Mono.empty();
        }

        return Mono.zip(
                        loanApplicationRepository.findLoanApplicationById(loanId)
                                .switchIfEmpty(Mono.error(new LoanApplicationNotFoundException(ErrorMessages.LOAN_APPLICATION_NOT_FOUND.getMessage()))),
                        statusRepository.findByName(statusFinal.toUpperCase())
                                .switchIfEmpty(Mono.error(new StatusNotFoundException(ErrorMessages.STATUS_NOT_FOUND.getMessage())))
                )
                .flatMap(tuple -> {
                    LoanApplication loanApp = tuple.getT1();
                    Status status = tuple.getT2();

                    loanApp.setStatus(status);
                    return loanApplicationRepository.saveLoanApplication(loanApp)
                            .doOnSuccess(loan -> logger.info("Updating request status {} in the database", loan.getId()))
                            .then();
                });
    }

    private boolean shouldSkipProcessing(String status, UUID loanId) {
        if (status == null || status.trim().isEmpty() || loanId == null) {
            logger.warn("Status null/empty {},  or loan_id null {}", status, loanId);
            return true;
        }
        if (!VALID_STATES_UPDATING.contains(status.toUpperCase())) {
            logger.warn("The status is not valid for updating in the database. Status: {}", status);
            return true;
        }
        return false;
    }
}
