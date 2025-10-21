package intern.lp.service.impl;


import intern.lp.dto.request.PaymentRequest;
import intern.lp.dto.response.PaymentResponse;
import intern.lp.entites.Payment;
import intern.lp.event.PaymentCreatedEvent;
import intern.lp.payment.PaymentMethod;
import intern.lp.payment.PaymentStatus;
import intern.lp.repository.PaymentRepository;
import intern.lp.service.EventPublisherService;
import intern.lp.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private  PaymentRepository paymentRepository;
    @Autowired
    private  EventPublisherService eventPublisher;


    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Map method
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (Exception e) {
            method = PaymentMethod.FAKE; // default fallback
        }

        // Decide status based on method
        PaymentStatus status;
        if (method == PaymentMethod.FAKE) {
            status = PaymentStatus.SUCCESS;
        } else if (method == PaymentMethod.COD) {
            status = PaymentStatus.PENDING;
        } else {
            // future providers default to PENDING
            status = PaymentStatus.PENDING;
        }

        // Create entity
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO)
                .method(method)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        // Publish event so Shipping/Order can react. Even COD -> publish (shipping may proceed with COD)
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .method(payment.getMethod().name())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .build();

        eventPublisher.publishPaymentCreated(event);

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        Payment p = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found: " + id));
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(Long orderId) {
        Payment p = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
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
