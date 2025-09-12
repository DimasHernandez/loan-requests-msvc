package co.com.pragma.model.loanvalidation.events.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PaymentPlanItem {

    private Integer month;

    private BigDecimal capital;

    private BigDecimal interestRate;

    private BigDecimal total;
}
