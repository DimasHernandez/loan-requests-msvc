package co.com.pragma.r2dbc.config;

import co.com.pragma.model.loanapplication.gateways.TransactionalWrapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
public class TransactionalWrapperImpl implements TransactionalWrapper {

    private final TransactionalOperator transactionalOperator;

    public TransactionalWrapperImpl(TransactionalOperator transactionalOperator) {
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public <T> Mono<T> transactional(Mono<T> publisher) {
        return publisher.as(transactionalOperator::transactional);
    }
}
