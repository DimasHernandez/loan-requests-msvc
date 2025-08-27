package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entities.LoanApplicationEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface LoanApplicationReactiveRepository extends ReactiveCrudRepository<LoanApplicationEntity, UUID>, ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

}
