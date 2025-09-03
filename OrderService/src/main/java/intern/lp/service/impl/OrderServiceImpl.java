package intern.lp.service.impl;

import intern.lp.dto.CustomerResponse;
import intern.lp.dto.OrderItemRequest;
import intern.lp.dto.OrderRequest;
import intern.lp.dto.ProductResponse;
import intern.lp.entites.Order;
import intern.lp.entites.OrderItem;
import intern.lp.enums.OrderStatus;
import intern.lp.repository.OrderRepository;
import intern.lp.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Order> getAllOrder() {
        return List.of();
    }

    @Override
    public String createOrder(OrderRequest orderRequest) {
        CustomerResponse customerResponse = getCustomerInfo(orderRequest.getCustomerId());

        if (!"SUCCESS".equals(customerResponse.getStatus())) {
            throw new RuntimeException("Customer not found: " + orderRequest.getCustomerId());
        }

        List<ProductResponse.Product> products = new ArrayList<>();

        for(OrderItem item : orderRequest.getOrderItems()){
            ProductResponse productResponse = getProductInfo(item.getProductId());
            log.info("Product found and status: {}",productResponse);
            if (!"SUCCESS".equals(productResponse.getStatus())) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }
            ProductResponse.Product product = productResponse.getProduct();
            products.add(product);
        }

        Order order = new Order();

        order.setCustomerId(orderRequest.getCustomerId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus( OrderStatus .PENDING);
        List<OrderItem> orderitems = new ArrayList<>();
        for(int i =0 ; i < orderRequest.getOrderItems().size(); i++){
            OrderItem itemRequest = orderRequest.getOrderItems().get(i);
            ProductResponse.Product product = products.get(i);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(itemRequest.getQuantity());

            orderitems.add(orderItem);
        }
        order.setOrderItems(orderitems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getId());
//
//        // Ở đây bạn sẽ thêm logic tạo order thật
//        // Giả lập tạo order thành công
//        String orderId = "ORDER_" + System.currentTimeMillis();
//        Long customerId = customerResponse.getCustomer().getId();
//        log.info("Customer found: {}", customerId);
//        orderRepository.save(new Order(customerId));
//
//        log.info("Order created successfully: {}", orderId);
        return savedOrder.getId().toString();
    }

    @Override
    public Order getOrderById(Long id) {
            return orderRepository.findById(id).orElse(null);
    }

    public CustomerResponse getCustomerInfo(Long customerId){
        try{
            Map<String, Object> request = new HashMap<>();
            request.put("action","GET_CUSTOMER");
            request.put("customerId", customerId);
            request.put("correlationId", UUID.randomUUID().toString());
            log.info("Sending request to CustomerService: {}", request);
            // Gửi message và nhận response
            Object response = rabbitTemplate.convertSendAndReceive(
                    "exchange_demo", // exchange (empty for default)
                    "customer.queue", // routing key
                    request
            );
            if(response instanceof Map){
                Map<String,Object> responseMap  = (Map<String, Object>) response;
                CustomerResponse customerResponse = new CustomerResponse();
                customerResponse.setStatus((String) responseMap.get("status"));
                if (responseMap.get("customer") instanceof Map) {
                    Map<String, Object> customerMap = (Map<String, Object>) responseMap.get("customer");
                    CustomerResponse.Customer customer = new CustomerResponse.Customer();
                    Long customerId1 = null;
                    Object customerIdObj =  customerMap.get("id");

                    // Xử lý casting an toàn
                    if (customerIdObj instanceof Integer) {
                        customerId1 = ((Integer) customerIdObj).longValue();
                    } else if (customerIdObj instanceof Long) {
                        customerId1 = (Long) customerIdObj;
                    } else if (customerIdObj instanceof String) {
                        customerId1 = Long.parseLong((String) customerIdObj);
                    }
                    customer.setId(customerId1);
                    customer.setName((String) customerMap.get("name"));
                    customer.setEmail((String) customerMap.get("email"));
                    customerResponse.setCustomer(customer);
                }

                return customerResponse;
            }
        }catch (Exception e) {
            log.error("Error calling CustomerService: {}", e.getMessage());
        }
        CustomerResponse errorResponse = new CustomerResponse();
        errorResponse.setStatus("ERROR");
        return errorResponse;
    }

    public ProductResponse getProductInfo(Long productId) {
        try{
            Map<String, Object> request = new HashMap<>();
            request.put("action","GET_PRODUCT");
            request.put("productId", productId);
            request.put("correlationId", UUID.randomUUID().toString());
            log.info("Sending request to ProductService: {}", request);
            Object response = rabbitTemplate.convertSendAndReceive("product.queue",request);
            if(response instanceof Map){
                Map<String,Object> responseMap  = (Map<String, Object>) response;
                return  convertToProductResponse(responseMap);
            }

        }catch (Exception e) {
            log.error("❌ Error calling ProductService: {}", e.getMessage(), e);
        }
        return createErrorProductResponse();
    }
    private ProductResponse convertToProductResponse(Map<String, Object> responseMap) {
        ProductResponse response = new ProductResponse();
        response.setStatus((String) responseMap.get("status"));
        if (responseMap.get("product") instanceof Map) {
            Map<String, Object> productMap = (Map<String, Object>) responseMap.get("product");
            ProductResponse.Product product= new ProductResponse.Product();

            Long productId= null;
            Object productIddObj = productMap.get("id");
            if (productIddObj instanceof Integer) {
                productId = Long.valueOf((Integer) productIddObj);
            } else if (productIddObj instanceof Long) {
                productId = (Long) productIddObj;
            } else if (productIddObj instanceof String) {
                productId = Long.parseLong((String) productIddObj);
            }
            product.setId(productId);


            product.setName((String) productMap.get("name"));
            product.setPrice(Double.parseDouble(String.valueOf(productMap.get("price"))));
            response.setProduct(product);
        }
        return response;
    }
    private ProductResponse createErrorProductResponse() {
        ProductResponse response = new ProductResponse();
        response.setStatus("ERROR");
        return response;
    }

}
