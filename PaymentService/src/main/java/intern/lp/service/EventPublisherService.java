package intern.lp.service;

import intern.lp.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final RabbitTemplate rabbitTemplate;

    // Hardcode giá trị hoặc dùng từ properties
    private static final String PAYMENT_EXCHANGE = "payment-exchange";
    private static final String PAYMENT_COMPLETED_KEY = "payment.completed";

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_COMPLETED_KEY, event);
            log.info("Payment completed event published for order {}, status: {}",
                    event.getOrderId(), event.getStatus());
        } catch (Exception e) {
            log.error("Failed to publish payment completed event for order {}: {}",
                    event.getOrderId(), e.getMessage());
            // Không throw exception để không ảnh hưởng đến response
        }
    }
}