package intern.lp.service;

import intern.lp.entites.Customer;
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
    private CustomerService customerService;

    @RabbitListener(queues = "customer")
    public Object handleMessage(Map<String, Object> request) {
        log.info("✅ Received request: {}", request);

        try{
            String action = request.get("action").toString();
            Long customerId = null;
            Object customerIdObj = request.get("customerId");

            // Xử lý casting an toàn
            if (customerIdObj instanceof Integer) {
                customerId = ((Integer) customerIdObj).longValue();
            } else if (customerIdObj instanceof Long) {
                customerId = (Long) customerIdObj;
            } else if (customerIdObj instanceof String) {
                customerId = Long.parseLong((String) customerIdObj);
            }

            log.debug("Converted customerId: {}", customerId);

            if("GET_CUSTOMER".equals(action)){
                Customer customer = customerService.getById(customerId)
                        .orElseThrow(() -> new RuntimeException("Customer not found"));

                Map<String, Object> response = new HashMap<>();
                response.put("status", "SUCCESS"); // Customer không null vì đã orElseThrow
                response.put("customer", customer);
                log.info("Customer send: {}", customer.toString());
                response.put("correlationId", request.get("correlationId"));

                log.info("📤 Sending success response: {}", response);
                return response; // QUAN TRỌNG: return ở đây
            }

        } catch (Exception e) {
            log.error("❌ Error processing message: {}", e.getMessage(), e);

            // Trả về response error cụ thể hơn
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("correlationId", request.get("correlationId"));
            return errorResponse;
        }

        // Nếu action không khớp
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", "Unknown action");
        errorResponse.put("correlationId", request.get("correlationId"));
        return errorResponse;
    }
}