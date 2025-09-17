package co.com.pragma.model.events.reports.gateway;

import co.com.pragma.model.events.reports.LoanReportEvent;
import reactor.core.publisher.Mono;

public interface LoanReportMessageGateway {

    Mono<String> sendToQueueApprovedLoanReport(LoanReportEvent loanReportEvent);
}
