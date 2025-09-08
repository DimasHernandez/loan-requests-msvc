package co.com.pragma.r2dbc.entities;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanReviewItemEntity {

    @Column("amount")
    private BigDecimal amount;

    @Column("term_month")
    private Integer termMonth;

    private String email;

    private String fullName;

    @Column("type_loan_name")
    private String loanTypeName;

    @Column("interest_rate")
    private BigDecimal interestRate;

    @Column("status_name")
    private String statusName;

    private Integer baseSalary;

    @Column("total_month_debt_approved_applications")
    private BigDecimal totalMonthlyDebtApprovedApplications;
}
