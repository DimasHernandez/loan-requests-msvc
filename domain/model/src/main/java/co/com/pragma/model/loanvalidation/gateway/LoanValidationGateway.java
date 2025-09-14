package co.com.pragma.model.loanvalidation.gateway;

import co.com.pragma.model.loanvalidation.events.request.LoanValidationRequest;
import reactor.core.publisher.Mono;

public interface LoanValidationGateway {

    Mono<String> sendToQueue(LoanValidationRequest request);
}
