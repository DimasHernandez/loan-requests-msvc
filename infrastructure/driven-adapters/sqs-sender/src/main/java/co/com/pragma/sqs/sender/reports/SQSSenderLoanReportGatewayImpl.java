package co.com.pragma.sqs.sender.reports;

import co.com.pragma.model.events.reports.LoanReportEvent;
import co.com.pragma.model.events.reports.gateway.LoanReportMessageGateway;
import co.com.pragma.sqs.sender.reports.config.SQSSenderLoanReportProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
public class SQSSenderLoanReportGatewayImpl implements LoanReportMessageGateway {

    private final SQSSenderLoanReportProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper mapper;

    public SQSSenderLoanReportGatewayImpl(SQSSenderLoanReportProperties properties,
                                          @Qualifier("configSqsLoanReport") SqsAsyncClient client,
                                          ObjectMapper mapper) {
        this.properties = properties;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public Mono<String> sendToQueueApprovedLoanReport(LoanReportEvent loanReportEvent) {
        return Mono.fromCallable(() -> buildRequest(loanReportEvent))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(LoanReportEvent loanReportEvent) throws JsonProcessingException {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(mapper.writeValueAsString(loanReportEvent))
                .build();
    }
}
