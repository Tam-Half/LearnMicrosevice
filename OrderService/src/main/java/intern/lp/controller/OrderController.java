package intern.lp.controller;

import intern.lp.dto.request.OrderRequest;
import intern.lp.dto.response.OrderResponse;
import intern.lp.service.impl.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderServiceImpl orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request){
        try {
                OrderResponse orderResponse = orderService.createOrder(request);

            return ResponseEntity.ok(orderResponse);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
