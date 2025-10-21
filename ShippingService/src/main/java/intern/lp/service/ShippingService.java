package intern.lp.service;

import intern.lp.dto.request.ShippingRequest;
import intern.lp.dto.response.ShippingResponse;
import intern.lp.entities.Shipping;
import intern.lp.events.OrderPaidEvent;
import intern.lp.repository.ShippingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepository shippingRepository;

    public ShippingResponse createShipping(ShippingRequest request) {
        Shipping shipping = new Shipping();
        shipping.setOrderId(request.getOrderId());
        shipping.setCustomerId(request.getCustomerId());
        shipping.setAddress(request.getAddress());
        shipping.setStatus("CREATED");
        shipping.setCreatedAt(LocalDateTime.now());
        shipping.setUpdatedAt(LocalDateTime.now());
        shippingRepository.save(shipping);

        log.info("Shipping created for order {}", request.getOrderId());
        return new ShippingResponse(shipping.getId(), shipping.getStatus());
    }

    @RabbitListener(queues = "shipping.queue")
    public void handleOrderPaidEvent(OrderPaidEvent event) {
        log.info("Received OrderPaidEvent for order {}", event.getOrderId());

        Shipping shipping = new Shipping();
        shipping.setOrderId(event.getOrderId());
        shipping.setCustomerId(event.getCustomerId());
        shipping.setStatus("CREATED");
        shipping.setCreatedAt(LocalDateTime.now());
        shipping.setUpdatedAt(LocalDateTime.now());
        shippingRepository.save(shipping);

        log.info("Shipping created via event for order {}", event.getOrderId());
    }
}
