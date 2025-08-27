package co.com.pragma.api;

import co.com.pragma.api.dto.LoanRequest;
import co.com.pragma.api.mapper.LoanMapper;
import co.com.pragma.usecase.loanapplication.LoanApplicationUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Handler {

    private final LoanApplicationUseCase loanApplicationUseCase;

    private final Validator validator;

    private final LoanMapper loanMapper;

    public Mono<ServerResponse> listenCreateLoanApplication(ServerRequest serverRequest) {
        URI uri = serverRequest.uri();

        return serverRequest.bodyToMono(LoanRequest.class)
                .flatMap(loanRequest -> {
                            Set<ConstraintViolation<LoanRequest>> violations = validator.validate(loanRequest);
                            if (!violations.isEmpty()) {
                                return Mono.error(new ConstraintViolationException(violations));
                            }
                            return Mono.just(loanRequest);
                        }
                )
                .map(loanMapper::toDomain)
                .flatMap(loanApplicationUseCase::saveLoanApplication)
                .map(loanMapper::toResponse)
                .flatMap(loanResponse -> ServerResponse
                        .created(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loanResponse));
    }
}
