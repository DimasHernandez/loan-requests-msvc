package co.com.pragma.r2dbc;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.r2dbc.entities.LoanTypeEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class LoanTypeReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanType,
        LoanTypeEntity,
        UUID,
        LoanTypeReactiveRepository
        > implements LoanTypeRepository {

    public LoanTypeReactiveRepositoryAdapter(LoanTypeReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, loanTypeEntity -> mapper.map(loanTypeEntity, LoanType.class));
    }

    @Override
    public Mono<LoanType> findByName(String name) {
        return repository.findByName(name)
                .map(super::toEntity);
    }
}
