package co.com.pragma.sqs.sender;

import co.com.pragma.model.loanapplication.UpdatedLoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanStatusMessageGateway;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
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
//@RequiredArgsConstructor
public class SQSSender implements LoanStatusMessageGateway {

    private final SqsAsyncClient client;
    private final SQSSenderProperties properties;
    private final ObjectMapper mapper;

    public SQSSender(SQSSenderProperties properties, @Qualifier("configSqs") SqsAsyncClient client, ObjectMapper mapper) {
        this.properties = properties;
        this.client = client;
        this.mapper = mapper;
    }

    public Mono<String> send(UpdatedLoanApplication event) {
        return Mono.fromCallable(() -> buildRequest(event))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(UpdatedLoanApplication event) throws JsonProcessingException {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(mapper.writeValueAsString(event))
                .build();
    }
}
