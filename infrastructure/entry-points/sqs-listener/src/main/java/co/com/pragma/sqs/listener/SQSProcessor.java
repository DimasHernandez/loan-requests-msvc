package co.com.pragma.sqs.listener;

import co.com.pragma.model.loanvalidation.events.response.LoanValidationResponse;
import co.com.pragma.usecase.loanvalidation.LoanValidationUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final ObjectMapper mapper;
    private final LoanValidationUseCase loanValidationUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        return Mono.fromCallable(() -> mapper.readValue(message.body(), Map.class))
                .map(json -> (String) json.get("Message"))
                .flatMap(raw -> {
                    try {
                        LoanValidationResponse response = mapper.readValue(raw, LoanValidationResponse.class);
                        return Mono.just(response);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error deserializing LoanValidationResponse", e));
                    }
                })
                .flatMap(loanValidationUseCase::processMessageResultQueue)
                .doOnSuccess(v -> log.info("Message processed successfully"))
                .doOnError(e -> log.warn("Error processing message: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

}
