package intern.lp.dto.request;

import lombok.*;

import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRequest {
    private Long orderId;
    private Long customerId;
    private String address;
    private List<OrderItemDTO> items;

}
