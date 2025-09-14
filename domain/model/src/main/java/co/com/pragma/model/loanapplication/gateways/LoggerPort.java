package co.com.pragma.model.loanapplication.gateways;

public interface LoggerPort {

    void info(String message, Object... args);

    void warn(String message, Object... args);

    void error(String message, Throwable throwable);
}
