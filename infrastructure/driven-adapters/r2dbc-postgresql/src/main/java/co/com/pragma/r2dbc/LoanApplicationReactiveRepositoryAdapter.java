package co.com.pragma.r2dbc;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanreviewitem.LoanReviewItem;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import co.com.pragma.r2dbc.entities.LoanApplicationEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public class LoanApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanApplication,
        LoanApplicationEntity,
        UUID,
        LoanApplicationReactiveRepository
        > implements LoanApplicationRepository {
    public LoanApplicationReactiveRepositoryAdapter(LoanApplicationReactiveRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, loanAppEntity -> {
            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanAppEntity.getId());
            loanApplication.setDocumentNumber(loanAppEntity.getDocumentNumber());
            loanApplication.setAmount(loanAppEntity.getAmount());
            loanApplication.setTermMonth(loanAppEntity.getTermMonth());
            loanApplication.setEmail(loanAppEntity.getEmail());

            LoanType loanType = LoanType.builder()
                    .id(loanAppEntity.getLoanTypeId())
                    .build();
            loanApplication.setLoanType(loanType);

            Status status = Status.builder()
                    .id(loanAppEntity.getStatusId())
                    .build();
            loanApplication.setStatus(status);
            return loanApplication;
        });
    }

    @Override
    protected LoanApplicationEntity toData(LoanApplication loanApplication) {

        // TODO: Feature -> For the status update, ensure that the parameters loanApplication.getLoanType().getId()
        //  loanApplication.getStatus().getId()
        LoanApplicationEntity loanAppEntity = new LoanApplicationEntity();
        loanAppEntity.setId(loanApplication.getId());
        loanAppEntity.setDocumentNumber(loanApplication.getDocumentNumber());
        loanAppEntity.setAmount(loanApplication.getAmount());
        loanAppEntity.setTermMonth(loanApplication.getTermMonth());
        loanAppEntity.setEmail(loanApplication.getEmail());
        loanAppEntity.setLoanTypeId(loanApplication.getLoanType().getId());
        loanAppEntity.setStatusId(loanApplication.getStatus().getId());
        return loanAppEntity;
    }

    @Override
    public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication) {
        return super.save(loanApplication);
    }

    @Override
    public Mono<LoanApplication> findLoanApplicationById(UUID id) {
        return super.findById(id);
    }

    @Override
    public Mono<Boolean> existsUserAndLoanTypeAndStatus(String documentNumber, UUID loanTypeId, UUID statusId) {
        return repository.existsByDocumentNumberAndLoanTypeIdAndStatusId(documentNumber, loanTypeId, statusId);
    }

    @Override
    public Flux<LoanReviewItem> findLoanApplicationWithDetails(List<String> statuses, int limit, int offset) {
        return repository.findLoanApplicationWithDetails(statuses, limit, offset)
                .map(loanReviewItemEntity -> mapper.map(loanReviewItemEntity, LoanReviewItem.class));
    }

    @Override
    public Mono<Long> countLoanApplicationByStatusesIn(List<String> statuses) {
        return repository.countByStatusesIn(statuses);
    }
}
