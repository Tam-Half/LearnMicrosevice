package intern.lp.dto.request;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShippingRequest implements Serializable {
    private Long orderId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private List<OrderItem> orderItems;

    @Data
    public static class OrderItem implements Serializable{
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal price;
    }
}
