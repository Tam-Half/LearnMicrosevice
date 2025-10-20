package intern.lp.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class InventoryResponse {
    private boolean available;
    private List<Long> unavailableItems;
}
