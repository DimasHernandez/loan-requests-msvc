package co.com.pragma.model.exceptions.enums;

public enum ErrorMessages {

    STATUS_NOT_FOUND("Estado no encontrado"),
    AMOUNT_OUT_OF_RANGE("El monto no es válido"),
    TERM_OUT_OF_RANGE("El plazo establecido no es válido"),
    LAMBDA_ERROR("Error invocando Lambda de capacidad"),
    USER_NOT_FOUND("Usuario no encontrado");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
