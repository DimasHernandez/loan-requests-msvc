package co.com.pragma.api.dto;

import java.util.UUID;

public record UpdateLoanApplicationResponse(

        UUID id,

        String email,

        String previousStatus,

        String newStatus,

        String message
) {
}
