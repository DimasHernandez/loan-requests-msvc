package co.com.pragma.model.loanvalidation.events.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ActiveLoanInfo {

    private UUID loanId;

    private BigDecimal amount;

    private BigDecimal interestRate;

    private Integer termMonth;
}
