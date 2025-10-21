package intern.lp.event;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentCreatedEvent {
    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private String status;
    private LocalDateTime createdAt;
}
