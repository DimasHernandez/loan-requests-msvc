package co.com.pragma.model.loanapplication;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplication {

    private UUID id;

    private String documentNumber;

    private BigDecimal amount;

    private Integer termMonth;

    private String email;

    private LoanType loanType;

    private Status status;
}
