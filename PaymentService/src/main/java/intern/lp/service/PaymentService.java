package intern.lp.service;

import intern.lp.dto.request.PaymentRequest;
import intern.lp.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse getPayment(Long id);
    PaymentResponse getByOrderId(Long orderId);
    void handlePaymentRequest(PaymentRequest request);
}
