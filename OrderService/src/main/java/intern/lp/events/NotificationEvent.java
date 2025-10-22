package intern.lp.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private Long orderId;
    private Long customerId;
    private String customerName;
    private String email;
    private String address;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String paymentStatus;
    private String shippingStatus;
    private List<String> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductItem {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
