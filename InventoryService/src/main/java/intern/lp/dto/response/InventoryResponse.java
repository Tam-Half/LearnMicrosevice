package intern.lp.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long orderId;
    private boolean available;
    private List<Long> unavailableItems;
}
