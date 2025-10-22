package intern.lp.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent {
    private Long orderId;
    private Long customerId;
    private String customerName;
    private String email;
    private Double totalAmount;
    private String message;
    private String status;
}
