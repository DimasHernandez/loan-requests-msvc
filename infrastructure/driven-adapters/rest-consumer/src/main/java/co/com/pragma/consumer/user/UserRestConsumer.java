package co.com.pragma.consumer.user;

import co.com.pragma.consumer.user.mapper.UserMapper;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRestConsumerPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
                .bodyToMono(UserInfoResponse.class)
                .map(userMapper::toEntity);
    }
}
