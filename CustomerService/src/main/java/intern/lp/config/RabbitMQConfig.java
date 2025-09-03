package intern.lp.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue customerQueue() {
        return new Queue("customer",true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return  new Jackson2JsonMessageConverter();
    }
}
