package intern.lp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import intern.lp.dto.request.OrderRequest;
import intern.lp.dto.request.ShippingRequest;
import intern.lp.entities.Shipping;
import intern.lp.repository.ShippingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    private final ShippingRepository shippingRepository;


    @RabbitListener(queues = "shipping.queue")
    public void createShipping(ShippingRequest order) {
        log.info("✅ Received ORDER from MQ: {}", order);

        Shipping shipping = Shipping.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .address(order.getShippingAddress())
                .orderItems(toJson(order.getOrderItems()))  // Convert list orderItems -> JSON string
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .status("DELIVERY")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        shippingRepository.save(shipping);
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}