package intern.lp.controller;

import intern.lp.dto.OrderRequest;
import intern.lp.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    private String apiKey = "aasd8989gf89sdg8s9f8s9f89sdf98d8f9dsf";
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest request){
        try {
            String orderId = orderService.createOrder(request);
            return ResponseEntity.ok("Order created: " + orderId);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}
