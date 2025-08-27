package co.com.pragma.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LoanRequest(

        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(max = 30, message = "El documento de identidad no debe superar los 30 caracteres")
        String documentNumber,

        @NotNull(message = "El monto del credito es obligatorio")
        @Positive(message = "El monto del credito debe ser un numero mayor a cero")
        BigDecimal amount,

        @NotNull(message = "El plazo del credito es obligatorio")
        @Positive(message = "El plazo del credito debe ser un numero mayor a cero")
        Integer termMonth,

        @NotBlank(message = "El tipo de credito es obligatorio")
        @Size(max = 150, message = "El nombre del tipo de crédito no debe superar los 150 caracteres")
        String loanTypeName
) {
}
