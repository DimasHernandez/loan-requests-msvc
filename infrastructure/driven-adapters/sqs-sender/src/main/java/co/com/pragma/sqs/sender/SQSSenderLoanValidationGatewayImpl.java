package co.com.pragma.sqs.sender;

import co.com.pragma.model.loanvalidation.events.request.LoanValidationRequest;
import co.com.pragma.model.loanvalidation.gateway.LoanValidationGateway;
import co.com.pragma.sqs.sender.config.SQSSenderLoanValidationProperties;
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
public class SQSSenderLoanValidationGatewayImpl implements LoanValidationGateway {

    private final SQSSenderLoanValidationProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper mapper;

    public SQSSenderLoanValidationGatewayImpl(SQSSenderLoanValidationProperties properties,
                                              @Qualifier("configSqsLoanValidation") SqsAsyncClient client,
                                              ObjectMapper mapper) {
        this.properties = properties;
        this.client = client;
        this.mapper = mapper;
    }


    @Override
    public Mono<String> sendToQueue(LoanValidationRequest event) {
        return Mono.fromCallable(() -> buildRequest(event))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(LoanValidationRequest event) throws JsonProcessingException {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(mapper.writeValueAsString(event))
                .build();
    }
}
