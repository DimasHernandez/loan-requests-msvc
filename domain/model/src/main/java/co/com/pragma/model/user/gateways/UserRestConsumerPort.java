package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRestConsumerPort {

    Mono<User> findUserByEmail(String email, String token);
}
