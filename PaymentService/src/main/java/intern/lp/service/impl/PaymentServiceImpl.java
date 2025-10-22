package intern.lp.service.impl;

import intern.lp.dto.request.PaymentRequest;
import intern.lp.dto.response.PaymentResponse;
import intern.lp.entites.Payment;
import intern.lp.event.PaymentCompletedEvent;
import intern.lp.payment.PaymentMethod;
import intern.lp.payment.PaymentStatus;
import intern.lp.repository.PaymentRepository;
import intern.lp.service.EventPublisherService;
import intern.lp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventPublisherService eventPublisher; // ✅ Inject EventPublisherService

    /**
     * ✅ LISTENER: Nhận payment request từ Order Service và trả về PaymentResponse
     */


    @Transactional
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "order.payment.create", durable = "true"),
            exchange = @Exchange(name = "payment-exchange", type = "direct"),
            key = "payment.create"
    ))
    public void handlePaymentRequest(PaymentRequest request) {
        log.info("Received payment request for order {}", request.getOrderId());

        // Lấy payment method, fallback về COD nếu null hoặc sai
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (Exception e) {
            method = PaymentMethod.COD;
            log.warn("Invalid payment method, fallback to COD");
        }

        // Kiểm tra xem payment đã tồn tại chưa (idempotency)
        var existingPayment = paymentRepository.findByOrderId(request.getOrderId());
        if (existingPayment.isPresent()) {
            log.info("Payment already exists for order {}, returning existing payment", request.getOrderId());
        }

        // Tạo payment với trạng thái PENDING
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO)
                .method(method)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment {} created with status PENDING", payment.getId());

        // Giả lập xử lý thanh toán
        boolean paymentSuccess = processPayment(payment);

        // Cập nhật trạng thái
        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            log.info("Payment {} processed successfully", payment.getId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Payment {} failed", payment.getId());
        }

        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // ✅ Publish event qua EventPublisherService
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .build();

        eventPublisher.publishPaymentCompleted(event);

        // ✅ Trả về PaymentResponse
//        return toResponse(payment);
    }

    /**
     * ✅ Giả lập xử lý thanh toán
     */
    private boolean processPayment(Payment payment) {
        try {
            // Giả lập delay xử lý thanh toán
            Thread.sleep(1000);

            // Logic xử lý thanh toán
            if (payment.getMethod() == PaymentMethod.COD) {
                // COD luôn thành công
                return true;
            } else if (payment.getMethod() == PaymentMethod.CREDIT_CARD) {
                // Giả lập 90% success rate cho thẻ
                return Math.random() < 0.9;
            } else {
                // Các phương thức khác
                return Math.random() < 0.85;
            }
        } catch (InterruptedException e) {
            log.error("Payment processing interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Wrapper method nếu cần gọi trực tiếp từ code
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(Long orderId) {
        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return toResponse(p);
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .amount(p.getAmount())
                .method(p.getMethod().name())
                .status(p.getStatus().name())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}