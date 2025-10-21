package intern.lp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod; // "COD", "VNPAY", "MOMO"...
}
