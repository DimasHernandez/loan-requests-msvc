package co.com.pragma.model.status.gateways;

import co.com.pragma.model.status.Status;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StatusRepository {

    Mono<Status> findByName(String name);

    Mono<Status> findStatusById(UUID id);
}
