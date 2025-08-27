package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.exceptions.LoandTypeNotFoundException;
import co.com.pragma.model.exceptions.StatusNotFoundException;
import co.com.pragma.model.exceptions.UserNotFoundException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.model.user.gateways.UserRestConsumerPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private static final String DEFAULT_STATUS_LOAN = "PENDING_REVIEW";

    private final LoanTypeRepository loanTypeRepository;
    private final StatusRepository statusRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRestConsumerPort userRestConsumerPort;

    public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication) {
        return userRestConsumerPort.findUserByDocumentIdentity(loanApplication.getDocumentNumber())
                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario no encontrado")))
                .flatMap(user -> {
                            loanApplication.setEmail(user.getEmail());
                            return assignLoanType(loanApplication);
                        }
                )
                .flatMap(this::assignStatus)
                .flatMap(loanApplicationRepository::saveLoanApplication);
    }

    private Mono<LoanApplication> assignLoanType(LoanApplication loanApplication) {
        return loanTypeRepository.findByName(loanApplication.getLoanType().getName())
                .switchIfEmpty(Mono.error(new LoandTypeNotFoundException("Tipo de prestamo no encontrado")))
                .map(loanType -> {
                    loanApplication.setLoanType(loanType);
                    return loanApplication;
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
}
