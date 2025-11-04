package intern.lp.service;

import intern.lp.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "productservice-service", url = "http://productservice-service.testing.svc.cluster.local")
public interface ProductService {

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);
}
