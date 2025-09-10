package co.com.pragma.model.loanapplication.gateways;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanreviewitem.LoanReviewItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanApplicationRepository {

    Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication);

    Mono<LoanApplication> findLoanApplicationById(UUID id);

    Mono<Boolean> existsUserAndLoanTypeAndStatus(String documentNumber, UUID loanTypeId, UUID statusId);

    Flux<LoanReviewItem> findLoanApplicationWithDetails(List<String> statuses, int limit, int offset);

    Mono<Long> countLoanApplicationByStatusesIn(List<String> statuses);
}
