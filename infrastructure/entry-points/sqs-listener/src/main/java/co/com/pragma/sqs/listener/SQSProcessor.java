package co.com.pragma.sqs.listener;

import co.com.pragma.usecase.loanapplication.LoanApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    // private final MyUseCase myUseCase;

    private final LoanApplicationUseCase loanApplicationUseCase;

    @Override
    public Mono<Void> apply(Message message) {

        System.out.println(message.body());
        return Mono.empty();
        // return myUseCase.doAny(message.body());
    }
}
