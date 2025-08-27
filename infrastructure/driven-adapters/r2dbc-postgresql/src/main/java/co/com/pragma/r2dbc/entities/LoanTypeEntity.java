package co.com.pragma.r2dbc.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table(name = "loans_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanTypeEntity {

    @Id
    @Column("loan_type_id")
    private UUID id;

    private String name;

    @Column("amount_min")
    private BigDecimal amountMin;

    @Column("amount_max")
    private BigDecimal amountMax;

    @Column("interest_rate")
    private BigDecimal interestRate;

    @Column("automatic_validation")
    private boolean automaticValidation;
}
