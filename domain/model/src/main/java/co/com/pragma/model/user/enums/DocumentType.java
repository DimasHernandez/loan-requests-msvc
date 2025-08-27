package co.com.pragma.model.user.enums;

public enum DocumentType {

    DNI("DNI", "Documento Nacional de Identidad"),
    CC("CC", "Cédula de Ciudadanía"),
    CE("CE", "Cédula de Extranjería"),
    PASSPORT("PASSPORT ", "Pasaporte"),
    OTHER("OTHER", "Otro");

    private final String code;
    private final String description;

    DocumentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
