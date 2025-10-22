package intern.lp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private List<ShippingItemDTO> orderItems;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
}