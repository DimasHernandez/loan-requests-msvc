package co.com.pragma.api;

import co.com.pragma.api.dto.LoanRequest;
import co.com.pragma.api.dto.UpdateLoanApplicationRequest;
import co.com.pragma.api.mapper.LoanMapper;
import co.com.pragma.usecase.loanapplication.LoanApplicationUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoanHandler {

    private final LoanApplicationUseCase loanApplicationUseCase;

    private final Validator validator;

    private final LoanMapper loanMapper;

    public Mono<ServerResponse> listenCreateLoanApplication(ServerRequest serverRequest) {
        URI uri = serverRequest.uri();
        return Mono.zip(
                        serverRequest.bodyToMono(LoanRequest.class)
                                .flatMap(this::validation),
                        serverRequest.principal().cast(Authentication.class)
                )
                .flatMap(tuple -> {
                            LoanRequest loanRequest = tuple.getT1();
                            Authentication auth = tuple.getT2();

                            String email = (String) auth.getPrincipal();
                            String token = (String) auth.getCredentials();
                            return loanApplicationUseCase.saveLoanApplication(
                                    loanMapper.toDomain(loanRequest),
                                    email, token
                            );
                        }
                )
                .map(loanMapper::toResponse)
                .flatMap(loanResponse -> ServerResponse
                        .created(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loanResponse));
    }

    public Mono<ServerResponse> listenGetLoanApplications(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);

        String token = request.headers().header(HttpHeaders.AUTHORIZATION).getFirst().substring(7);
        List<String> statuses = request.queryParam("statuses").map(status -> Arrays.asList(status.split(",")))
                .orElse(List.of("PENDING_REVIEW"));

        return loanApplicationUseCase.getLoanApplicationsForReview(statuses, page, size, token)
                .flatMap(pageResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(pageResponse));
    }

    public Mono<ServerResponse> listenUpdateLoanApplication(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("loanApplicationId"));
        return request.bodyToMono(UpdateLoanApplicationRequest.class)
                .flatMap(this::validation)
                .flatMap(updateLoanRequest ->
                        loanApplicationUseCase.updatedLoanApplicationStatus(id, updateLoanRequest.status().toUpperCase()))
                .map(loanMapper::toUpdateLoanApplicationResponse)
                .flatMap(loanResponse -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loanResponse));
    }

    private <T> Mono<T> validation(T entityDto) {
        Set<ConstraintViolation<T>> violations = validator.validate(entityDto);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }
        return Mono.just(entityDto);
    }
}
