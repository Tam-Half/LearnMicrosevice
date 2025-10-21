package intern.lp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}