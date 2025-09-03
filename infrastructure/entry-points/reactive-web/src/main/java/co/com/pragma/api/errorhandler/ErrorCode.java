package co.com.pragma.api.errorhandler;

public enum ErrorCode {

    // 422
    INVALID_CREDENTIALS("AUTH_001", "Credenciales inválidas"),
    EMAIL_ALREADY_REGISTERED("USR_001", "Email ya registrado"),
    FIELD_EMPTY("USR_002", "Campo obligatorio vacío"),
    INVALID_EMAIL_FORMAT("USR_003", "Formato de email inválido"),
    ACCESS_DENIED("USR_004", "No puedes crear préstamos a nombre de otro usuario"),
    AMOUNT_OUT_RANGE("AMT_001", "Monto fuera del rango establecido"),
    TERM_OUT_RANGE("TERM_001", "Plazo fuera del rango establecido"),
    LOAN_REQUEST_STATUS_MISMATCH("USR_005", "El usuario ya cuenta con una solicitud de préstamo en proceso del mismo tipo"),

    // 400
    MALFORMED_JSON("GEN_001", "Formato JSON inválido"),
    BAD_REQUEST("GEN_002", "Petición malformada"),
    INVALID_DOCUMENT_TYPE("GEN_003", "Tipo de documento no soportado"),

    // 409
    DOCUMENT_ALREADY_EXISTS("DOC_001", "El número de documento ya existe"),
    GENERIC_CONFLICT("GEN_409", "Conflicto con los datos existentes"),

    // 404
    USER_NOT_FOUND("USR_004", "Usuario no encontrado"),
    ROLE_NOT_FOUND("ROL_001", "Rol no encontrado"),
    LOAN_TYPE_NOT_FOUND("LOAN_TYPE_001", "Loan type no encontrado"),
    STATUS_NOT_FOUND("STATUS_001", "Estado no encontrado"),

    //500
    GENERIC_SERVER_ERROR("GEN_500", "Error interno del servidor"),
    SERVICE_UNAVAILABLE("GEN_503", "Servicio no disponible");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
