package intern.lp.events;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Data
public class OrderPaidEvent {
    private Long orderId;
    private Long customerId;
}
