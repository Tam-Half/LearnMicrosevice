package intern.lp.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {
    private Long orderId;
    private List<OrderItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {  // ✅ phải là static class
        private Long productId;
        private Integer quantity;
    }
}
