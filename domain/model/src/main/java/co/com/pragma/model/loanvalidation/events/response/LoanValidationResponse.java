package co.com.pragma.model.loanvalidation.events.response;


import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanValidationResponse {

    private UUID loanId;

    private String result;

    private List<PaymentPlanItem> paymentPlanItems;
}
