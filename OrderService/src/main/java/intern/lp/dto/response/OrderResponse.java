package intern.lp.dto.response;

import intern.lp.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private String customerName;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
}