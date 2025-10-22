package intern.lp.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor // <-- Tạo constructor rỗng
public class OrderRequest {

    private Long orderId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private List<OrderItem> orderItems;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;

    @NoArgsConstructor
    public static class OrderItem {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal price;
    }

}