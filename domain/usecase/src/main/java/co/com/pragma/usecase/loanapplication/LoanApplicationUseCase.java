package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.exceptions.*;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoggerPort;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.model.user.gateways.UserRestConsumerPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private static final String DEFAULT_STATUS_LOAN = "PENDING_REVIEW";

    private final LoanTypeRepository loanTypeRepository;
    private final StatusRepository statusRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRestConsumerPort userRestConsumerPort;
    private final LoggerPort logger;

    public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication) {
        return searchUserAndAssignEmail(loanApplication)
                .flatMap(this::assignLoanType)
                .flatMap(this::assignStatus)
                .flatMap(this::validateLoanApplicationStateAndType)
                .flatMap(loanApplicationRepository::saveLoanApplication)
                .map(savedLoanApp -> {
                    loanApplication.setId(savedLoanApp.getId());
                    return loanApplication;
                })
                .doOnSuccess(loanApp ->
                        logger.info("Loan application registered. Status: {}, Document: {}", DEFAULT_STATUS_LOAN,
                                loanApp.getDocumentNumber()));
    }

    private Mono<LoanApplication> searchUserAndAssignEmail(LoanApplication loanApplication) {
        return userRestConsumerPort.findUserByDocumentIdentity(loanApplication.getDocumentNumber())
                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario no encontrado")))
                .map(user -> {
                            loanApplication.setEmail(user.getEmail());
                            return loanApplication;
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
                .flatMap(isValid -> {
                            if (Boolean.TRUE.equals(isValid)) {
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
}
