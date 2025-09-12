package co.com.pragma.usecase.loanvalidation;

import co.com.pragma.model.exceptions.StatusNotFoundException;
import co.com.pragma.model.exceptions.enums.ErrorMessages;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoggerPort;
import co.com.pragma.model.loanvalidation.events.request.LoanValidationRequest;
import co.com.pragma.model.loanvalidation.gateway.LoanValidationGateway;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.model.user.User;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanValidationUseCase {

    private static final String STATUS_PENDING_VALIDATION = "PENDING_VALIDATION";
    private static final String STATUS_APPROVED = "APPROVED";

    private final LoanValidationGateway loanValidationGateway;
    private final LoanApplicationRepository loanApplicationRepository;
    private final StatusRepository statusRepository;
    private final LoggerPort logger;

    public Mono<LoanApplication> enqueueLoanValidation(LoanApplication loanApp, User user) {
        return loanApplicationRepository.findLoanApplicationByDocumentNumberAndStatus(user.getDocumentNumber(), STATUS_APPROVED)
                .collectList()
                .flatMap(approvedLoans ->
                        statusRepository.findByName(STATUS_PENDING_VALIDATION)
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


//    private Mono<UpdatedLoanApplication> updateLoanStatus(LoanApplication loanApp, LoanValidationResponse response) {
//        String finalStatusName = response.getResult();
//
//        return transactionalWrapper.transactional(
//                statusRepository.findByName(finalStatusName)
//                        .switchIfEmpty(Mono.error(new StatusNotFoundException(ErrorMessages.STATUS_NOT_FOUND.getMessage())))
//                        .flatMap(newStatus -> {
//                            loanApp.setStatus(newStatus);
//                            return loanApplicationRepository.saveLoanApplication(loanApp)
//                                    .map(loanUpdated -> UpdatedLoanApplication.builder()
//                                            .id(loanUpdated.getId())
//                                            .email(loanUpdated.getEmail())
//                                            .previousStatus(STATUS_PENDING_REVIEW)
//                                            .newStatus(newStatus.getName())
//                                            .message(MESSAGE_LOAN_VALIDATION)
//                                            .build());
//                        })
//        );
//
//    }
}
