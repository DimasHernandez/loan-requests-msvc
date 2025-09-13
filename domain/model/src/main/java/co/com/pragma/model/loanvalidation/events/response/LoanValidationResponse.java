package co.com.pragma.model.loanvalidation.events.response;


import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class LoanValidationResponse {

    private UUID loanId;

    private String documentNumber;

    private String fullName;

    private String result;

    private List<PaymentPlanItem> paymentPlanItems;
}
