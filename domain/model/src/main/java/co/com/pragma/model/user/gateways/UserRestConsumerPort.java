package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;
import co.com.pragma.model.userbasicinfo.UserBasicInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRestConsumerPort {

    Mono<User> findUserByEmail(String email, String token);

    Flux<UserBasicInfo> findUsersByBatchEmails(List<String> emails, String token);
}
