package co.com.pragma.r2dbc.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplicationEntity {

    @Id
    @Column("loan_app_id")
    private UUID id;

    @Column("document_number")
    private String documentNumber;

    private BigDecimal amount;

    @Column("term_month")
    private Integer termMonth;

    private String email;

    @Column("loan_type_id")
    private UUID loanTypeId;

    @Column("status_id")
    private UUID statusId;

    @Column("total_month_debt_approved_applications")
    private BigDecimal totalMonthlyDebtApprovedApplications;
}
