package intern.lp.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitConfig {

    @Value("${payment.exchange}")
    private String paymentExchange;

    @Value("${payment.routing.created}")
    private String paymentCreatedRoutingKey;

    @Value("${payment.queue.created}")
    private String paymentCreatedQueue;

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(paymentExchange);
    }

    @Bean
    public Queue paymentCreatedQueue() {
        return new Queue(paymentCreatedQueue, true);
    }

    @Bean
    public Binding paymentCreatedBinding() {
        return BindingBuilder.bind(paymentCreatedQueue())
                .to(paymentExchange())
                .with(paymentCreatedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
