package co.com.pragma.api.dto;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanResponse(
        UUID id,

        String documentNumber,

        BigDecimal amount,

        Integer termMonth,

        String email,

        LoanType loanType,

        Status status,

        BigDecimal totalMonthlyDebtApprovedApplications
) {
}
