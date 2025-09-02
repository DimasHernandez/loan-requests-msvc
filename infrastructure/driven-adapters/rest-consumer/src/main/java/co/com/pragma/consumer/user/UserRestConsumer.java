package co.com.pragma.consumer.user;

import co.com.pragma.consumer.exception.ServiceInavailableException;
import co.com.pragma.consumer.user.errorclient.ErrorClient;
import co.com.pragma.consumer.user.mapper.UserMapper;
import co.com.pragma.model.exceptions.UserNotFoundException;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRestConsumerPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserRestConsumer implements UserRestConsumerPort {

    private final WebClient webClient;

    private final UserMapper userMapper;

    @CircuitBreaker(name = "findUserByDocumentIdentity")
    @Override
    public Mono<User> findUserByDocumentIdentity(String documentNumber) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/users/{documentNumber}")
                        .build(documentNumber)
                )
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxServerError)
                .bodyToMono(UserInfoResponse.class)
                .map(userMapper::toEntity);
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
                        return Mono.error(new ServiceInavailableException(errorClient.detail()));
                    }
                    return Mono.error(new RuntimeException(errorClient.detail()));
                });
    }
}
