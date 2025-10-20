package intern.lp.dto.request;

import lombok.Data;

@Data
public class Product {
    private Long id;
    private String name;
    private double price;
    private String  description;
}
