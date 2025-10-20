package intern.lp.dto.request;

import lombok.Data;

@Data
public class ProductRequest {
    private Long id;
    private String name;
    private double price;
    private String  description;
}
