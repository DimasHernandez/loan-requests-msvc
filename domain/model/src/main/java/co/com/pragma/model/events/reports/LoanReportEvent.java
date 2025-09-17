package co.com.pragma.model.events.reports;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanReportEvent {

    private UUID loanId;

    private String documentNumber;

    private String status;

    private BigDecimal amount;

    private LocalDateTime updatedAt;
}
