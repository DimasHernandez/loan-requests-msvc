package co.com.pragma.consumer.exception;

public class ServiceInavailableException extends RuntimeException {

    public ServiceInavailableException(String message) {
        super(message);
    }
}
