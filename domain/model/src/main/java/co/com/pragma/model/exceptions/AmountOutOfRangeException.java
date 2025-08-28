package co.com.pragma.model.exceptions;

public class AmountOutOfRangeException extends RuntimeException {

    public AmountOutOfRangeException(String message) {
        super(message);
    }
}
