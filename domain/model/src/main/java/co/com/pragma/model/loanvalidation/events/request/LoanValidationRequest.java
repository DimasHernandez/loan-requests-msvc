package co.com.pragma.model.loanvalidation.events.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanValidationRequest {

    private UUID loanId;

    private String documentNumber;

    private String fullName;

    private BigDecimal amount;

    private Integer termMonth;

    private BigDecimal interestRate;

    private Integer baseSalary;

    private List<ActiveLoanInfo> activeLoans;
}
