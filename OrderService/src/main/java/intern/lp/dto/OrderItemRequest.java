package intern.lp.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderItemRequest {
    private Long id;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}
