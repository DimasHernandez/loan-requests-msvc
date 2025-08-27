package co.com.pragma.model.loanapplication.gateways;

import co.com.pragma.model.loanapplication.LoanApplication;
import reactor.core.publisher.Mono;

public interface LoanApplicationRepository {

    Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication);
}
