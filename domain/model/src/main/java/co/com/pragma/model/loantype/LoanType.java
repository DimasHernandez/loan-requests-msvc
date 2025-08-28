package co.com.pragma.model.loantype;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {

    private UUID id;

    private String name;

    private BigDecimal amountMin;

    private BigDecimal amountMax;

    private Integer termMonthMin;

    private Integer termMonthMax;

    private BigDecimal interestRate;

    private boolean automaticValidation;
}
