package co.com.pragma.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateLoanApplicationRequest(

        @NotBlank(message = "El campo estado es obligatorio")
        String status
) {
}
