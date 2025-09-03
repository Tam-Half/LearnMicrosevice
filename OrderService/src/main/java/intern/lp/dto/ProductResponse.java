package intern.lp.dto;

import lombok.Data;

@Data
public class ProductResponse {
    private String status;
    private Product product;

    @Data
    public static class Product {
        private Long id;
        private String name;
        private int namsx;
        private double price;
    }
}
