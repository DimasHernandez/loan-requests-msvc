package co.com.pragma.consumer.user;

import co.com.pragma.consumer.exception.ServiceUnavailableException;
import co.com.pragma.consumer.user.errorclient.ErrorClient;
import co.com.pragma.consumer.user.mapper.UserMapper;
import co.com.pragma.model.exceptions.UserNotFoundException;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRestConsumerPort;
import co.com.pragma.model.userbasicinfo.UserBasicInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRestConsumer implements UserRestConsumerPort {

    private final WebClient webClient;

    private final UserMapper userMapper;

    @CircuitBreaker(name = "findUserByEmail")
    @Override
    public Mono<User> findUserByEmail(String email, String token) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/users/email/{email}")
                        .build(email)
                )
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxServerError)
                .bodyToMono(UserInfoResponse.class)
                .map(userMapper::toEntity);
    }

    @CircuitBreaker(name = "findUsersByBatchEmails")
    @Override
    public Flux<UserBasicInfo> findUsersByBatchEmails(List<String> emails, String token) {
        return Flux.fromIterable(emails)
                .buffer(1000)
                .flatMap(batch -> callBatchEndpoint(batch, token))
                .flatMap(Flux::fromIterable);
    }

    private Mono<List<UserBasicInfo>> callBatchEndpoint(List<String> batch, String token) {
        // Create the request body as Mono
        Mono<EmailRequest> emailRequestMono = Mono.just(new EmailRequest(batch));

        return webClient.post()
                .uri("/api/v1/users/emails/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .body(emailRequestMono, EmailRequest.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxServerError)
                .bodyToFlux(UserBasicInfoResponse.class)
                .map(userMapper::toBasicInfo)
                .collectList();
    }

    private Mono<? extends Throwable> handle4xxClientError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ErrorClient.class)
                .flatMap(errorClient -> {

                    HttpStatusCode status = clientResponse.statusCode();

                    if (status == HttpStatus.NOT_FOUND) {
                        return Mono.error(new UserNotFoundException(errorClient.detail()));
                    } else if (status == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new IllegalArgumentException(errorClient.detail()));
                    } else {
                        return Mono.error(new RuntimeException(errorClient.detail()));
                    }
                });
    }

    private Mono<? extends Throwable> handle5xxServerError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ErrorClient.class)
                .flatMap(errorClient -> {

                    HttpStatusCode status = clientResponse.statusCode();
                    if (status == HttpStatus.SERVICE_UNAVAILABLE) {
                        return Mono.error(new ServiceUnavailableException(errorClient.detail()));
                    }
                    return Mono.error(new RuntimeException(errorClient.detail()));
                });
    }
}
