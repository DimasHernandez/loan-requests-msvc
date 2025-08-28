package co.com.pragma.model.exceptions;

public class LoanTypeNotFoundException extends RuntimeException {

    public LoanTypeNotFoundException(String message) {
        super(message);
    }
}
