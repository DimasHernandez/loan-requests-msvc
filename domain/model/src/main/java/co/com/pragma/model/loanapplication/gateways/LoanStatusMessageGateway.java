package co.com.pragma.model.loanapplication.gateways;

import co.com.pragma.model.loanapplication.UpdatedLoanApplication;
import reactor.core.publisher.Mono;

public interface LoanStatusMessageGateway {

    Mono<String> send(UpdatedLoanApplication updatedLoanApplication);
}
