package intern.lp.service;

import intern.lp.event.PaymentCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public EventPublisherService(RabbitTemplate rabbitTemplate,
                                 @Value("${payment.exchange}") String exchange,
                                 @Value("${payment.routing.created}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishPaymentCreated(PaymentCreatedEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
