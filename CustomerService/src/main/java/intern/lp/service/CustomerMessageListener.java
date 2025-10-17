package intern.lp.service;

import intern.lp.entites.Customer;
import intern.lp.service.impl.CustomerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CustomerMessageListener {

    @Autowired
    private CustomerServiceImpl customerService;

    @RabbitListener(queues = "customer")
    public Object handleMessage(Map<String, Object> request) {
        return null;
    }
}
