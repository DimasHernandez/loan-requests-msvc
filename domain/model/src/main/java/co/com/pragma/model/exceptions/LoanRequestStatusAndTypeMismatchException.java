package co.com.pragma.model.exceptions;

public class LoanRequestStatusAndTypeMismatchException extends RuntimeException {

    public LoanRequestStatusAndTypeMismatchException(String message) {
        super(message);
    }
}
