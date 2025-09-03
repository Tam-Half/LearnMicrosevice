package intern.lp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
//    @Value("${rabbitmq.queue.name}")
//    private String queue;
//
//    @Value("${rabbitmq.exchange.name}")
//    private String exchange;
//
//    @Value("${rabbitmq.routing.key.name}")
//    private String routingKey;
//    //spring bean for rabbitmq queue
//    @Bean
//    public Queue queue() {
//        return  new Queue(queue);
//    }
//
//    @Bean
//    public TopicExchange exchange() {
//        return  new TopicExchange(exchange);
//    }
//
//    @Bean
//    public Binding blinding(){
//        return BindingBuilder.bind(queue())
//                .to(exchange())
//                .with(routingKey);
//    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return  new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setReplyTimeout(10000);
        return rabbitTemplate;
    }
}
