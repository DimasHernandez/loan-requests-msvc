package co.com.pragma.r2dbc;

import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.r2dbc.entities.StatusEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class StatusReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Status,
        StatusEntity,
        UUID,
        StatusReactiveRepository
        > implements StatusRepository {

    public StatusReactiveRepositoryAdapter(StatusReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, statusEntity -> mapper.map(statusEntity, Status.class));
    }

    @Override
    public Mono<Status> findByName(String name) {
        return repository.findByName(name)
                .map(super::toEntity);
    }

    @Override
    public Mono<Status> findStatusById(UUID id) {
        return repository.findById(id)
                .map(super::toEntity);
    }
}
