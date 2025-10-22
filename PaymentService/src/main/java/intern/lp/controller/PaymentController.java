package intern.lp.controller;


import intern.lp.dto.request.PaymentRequest;
import intern.lp.dto.response.PaymentResponse;
import intern.lp.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private  PaymentService paymentService;


//    @PostMapping
//    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest req) {
//        PaymentResponse resp = paymentService.handlePaymentRequest(req);
//        return ResponseEntity.ok(resp);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }
}
