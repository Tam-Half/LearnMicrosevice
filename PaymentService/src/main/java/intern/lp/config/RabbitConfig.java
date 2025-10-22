package intern.lp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
@Configuration
public class RabbitConfig {



    // Exchange
    public static final String PAYMENT_EXCHANGE = "payment-exchange";

    // --- Queue Names ---
    // ✅ THÊM HẰNG SỐ NÀY
    public static final String PAYMENT_CREATE_QUEUE = "order.payment.create";

    // (Queue này bạn đã có)
    public static final String ORDER_PAYMENT_QUEUE = "order.payment.completed";

    // --- Routing Keys ---
    public static final String PAYMENT_CREATE_KEY = "payment.create";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";

    // ... (Bean 'jsonMessageConverter' và 'rabbitTemplate' giữ nguyên) ...

    /**
     * ✅ Declare Exchange
     */
    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE, true, false);
    }

    /**
     * ✅ Declare Queues
     */

    // (Queue này bạn đã có)
    @Bean
    public Queue orderPaymentQueue() {
        return new Queue(ORDER_PAYMENT_QUEUE, true);
    }

    // ✅ THÊM BEAN NÀY
    // Tên phương thức (paymentRequestQueue) phải khớp với
    // tên tham số trong 'paymentRequestBinding'
    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(PAYMENT_CREATE_QUEUE, true);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        // Dòng quan trọng: Set converter cho listener
        factory.setMessageConverter(messageConverter);

        return factory;
    }
    /**
     *
     * ✅ Bindings
     */

    // Bây giờ phương thức này sẽ chạy được
    // vì Spring tìm thấy bean tên 'paymentRequestQueue'
    @Bean
    public Binding paymentRequestBinding(Queue paymentRequestQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentRequestQueue)
                .to(paymentExchange)
                .with(PAYMENT_CREATE_KEY);
    }

    // (Binding này của bạn đã đúng, giữ nguyên)
    @Bean
    public Binding orderPaymentBinding(Queue orderPaymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(orderPaymentQueue)
                .to(paymentExchange)
                .with(PAYMENT_COMPLETED_KEY);
    }
}