package intern.lp.dto.request;

import intern.lp.entites.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class InventoryRequest {
    private Long orderId;
    private List<OrderItem> items;
}