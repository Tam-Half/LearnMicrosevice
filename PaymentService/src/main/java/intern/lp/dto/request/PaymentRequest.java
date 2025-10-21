package intern.lp.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod; // FAKE, COD, ...
    private String currency; // optional
}