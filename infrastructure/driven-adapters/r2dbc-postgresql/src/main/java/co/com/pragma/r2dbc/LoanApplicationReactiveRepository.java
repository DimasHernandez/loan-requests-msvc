package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entities.LoanApplicationEntity;
import co.com.pragma.r2dbc.entities.LoanReviewItemEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanApplicationReactiveRepository extends ReactiveCrudRepository<LoanApplicationEntity, UUID>, ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

    Mono<Boolean> existsByDocumentNumberAndLoanTypeIdAndStatusId(String documentNumber, UUID loanTypeId, UUID status);

    @Query("""
            SELECT la.amount, la.term_month, la.email, la.total_month_debt_approved_applications,
                    s.name AS status_name,
                    lt.name AS type_loan_name,
                    lt.interest_rate
                FROM loan_applications la
            JOIN statuses s ON la.status_id = s.status_id
            JOIN loans_types lt ON la.loan_type_id = lt.loan_type_id
            WHERE s.name IN (:statuses)
            LIMIT :limit OFFSET :offset
            """)
    Flux<LoanReviewItemEntity> findLoanApplicationWithDetails(List<String> statuses, int limit, int offset);

    @Query("""
             SELECT COUNT(*) FROM loan_applications la
                JOIN statuses s ON la.status_id = s.status_id
                WHERE s.name IN (:statuses)
            """)
    Mono<Long> countByStatusesIn(List<String> statuses);

}
