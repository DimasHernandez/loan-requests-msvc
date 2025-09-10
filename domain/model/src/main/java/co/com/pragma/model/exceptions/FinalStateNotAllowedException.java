package co.com.pragma.model.exceptions;

public class FinalStateNotAllowedException extends RuntimeException {

    public FinalStateNotAllowedException(String message) {
        super(message);
    }
}
