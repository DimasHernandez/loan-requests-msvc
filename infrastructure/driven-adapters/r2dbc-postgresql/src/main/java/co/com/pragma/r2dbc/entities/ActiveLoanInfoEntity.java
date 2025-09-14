package co.com.pragma.r2dbc.entities;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ActiveLoanInfoEntity {

    @Column("loan_id")
    private UUID loanId;

    private BigDecimal amount;

    @Column("interest_rate")
    private BigDecimal interestRate;

    @Column("term_month")
    private Integer termMonth;
}
