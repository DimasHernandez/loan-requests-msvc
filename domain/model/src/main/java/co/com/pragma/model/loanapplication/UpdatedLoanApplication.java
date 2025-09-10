package co.com.pragma.model.loanapplication;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdatedLoanApplication {

    private UUID id;

    private String email;

    private String previousStatus;

    private String newStatus;

    private String message;
}
