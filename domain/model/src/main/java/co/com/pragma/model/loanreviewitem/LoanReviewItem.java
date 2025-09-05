package co.com.pragma.model.loanreviewitem;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class LoanReviewItem {

    private BigDecimal amount;

    private Integer termMonth;

    private String email;

    private String fullName;

    private String loanTypeName;

    private BigDecimal interestRate;

    private String statusName;

    private Integer baseSalary;

    private BigDecimal totalMonthlyDebtApprovedApplications;
}
