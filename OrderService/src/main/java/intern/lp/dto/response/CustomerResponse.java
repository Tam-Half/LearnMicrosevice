package intern.lp.dto.response;

import lombok.Data;

@Data
public class CustomerResponse {
    private String status;
    private Customer customer;

    @Data
    public static class Customer {
        private String fullName;
        private String email;
        private String phone;
        private String address;
    }
}
