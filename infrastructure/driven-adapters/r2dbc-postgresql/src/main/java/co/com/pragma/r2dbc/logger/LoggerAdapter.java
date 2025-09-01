package co.com.pragma.r2dbc.logger;

import co.com.pragma.model.loanapplication.gateways.LoggerPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggerAdapter implements LoggerPort {

    @Override
    public void info(String message, Object... args) {
        log.info(message, args);
    }
}
