package intern.lp.service;

import intern.lp.dto.response.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name= "customer-service")
public interface CustomerService {

    @GetMapping("/api/customers/{id}")
    CustomerResponse getCustomerById(@PathVariable("customer") Long id);
}
