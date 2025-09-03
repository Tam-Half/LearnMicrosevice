package intern.lp.dto;

import lombok.Data;

@Data
public class CustomerResponse {
    private String status;
    private Customer customer;

    @Data
    public static class Customer {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
    }
}
