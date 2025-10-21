package intern.lp.service.impl;

import intern.lp.dto.request.InventoryRequest;
import intern.lp.dto.request.OrderRequest;
import intern.lp.dto.request.PaymentRequest;
import intern.lp.dto.response.*;
import intern.lp.entites.Order;
import intern.lp.entites.OrderItem;
import intern.lp.enums.OrderStatus;
import intern.lp.repository.OrderRepository;
import intern.lp.service.CustomerService;
import intern.lp.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl {

    @Autowired
    private ProductService productClient;

    @Autowired
    private CustomerService customerClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Exchange & Routing key cho Inventory
    private static final String INVENTORY_EXCHANGE = "inventory-exchange";
    private static final String INVENTORY_ROUTING_KEY = "inventory.check";

    // Exchange & Routing key cho Payment
    private static final String PAYMENT_EXCHANGE = "payment-exchange";
    private static final String PAYMENT_ROUTING_KEY = "payment.create";

    public OrderResponse createOrder(OrderRequest orderRequest) {

        // ----------- BƯỚC 1: Lấy product và customer -----------
        List<ProductResponse> productInfos = orderRequest.getOrderItems().stream()
                .map(item -> productClient.getProductById(item.getProductId()))
                .collect(Collectors.toList());

        CustomerResponse customerResponse = customerClient.getCustomerById(orderRequest.getCustomerId());
        if (customerResponse == null) {
            throw new RuntimeException("Customer not found with id: " + orderRequest.getCustomerId());
        }

        // ----------- BƯỚC 2: Tính tổng tiền -----------
        BigDecimal totalAmount = productInfos.stream()
                .map(p -> {
                    int qty = orderRequest.getOrderItems().stream()
                            .filter(i -> i.getProductId().equals(p.getId()))
                            .findFirst().get().getQuantity();
                    return BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ----------- BƯỚC 3: Lưu order PENDING -----------
        Order order = new Order();
        order.setCustomerId(orderRequest.getCustomerId());
        order.setOrderItems(orderRequest.getOrderItems().stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            ProductResponse product = productInfos.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElseThrow();
            orderItem.setPrice(BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
            return orderItem;
        }).collect(Collectors.toList()));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        log.info("Order {} saved as PENDING", savedOrder.getId());

        // ----------- BƯỚC 4: Kiểm tra tồn kho -----------
        InventoryRequest inventoryRequest = new InventoryRequest(savedOrder.getId(), orderRequest.getOrderItems());
        InventoryResponse inventoryResponse = (InventoryResponse) rabbitTemplate.convertSendAndReceive(
                INVENTORY_EXCHANGE, INVENTORY_ROUTING_KEY, inventoryRequest);

        if (inventoryResponse == null || !inventoryResponse.isAvailable()) {
            orderRepository.deleteById(savedOrder.getId());
            throw new RuntimeException("Order failed: Not enough stock!");
        }

        // ----------- BƯỚC 5: Gửi PaymentRequest qua RabbitMQ -----------
        PaymentRequest paymentRequest = new PaymentRequest(savedOrder.getId(), totalAmount, "COD");
        PaymentResponse paymentResponse = (PaymentResponse) rabbitTemplate.convertSendAndReceive(
                PAYMENT_EXCHANGE, PAYMENT_ROUTING_KEY, paymentRequest);

        if (paymentResponse == null || !"SUCCESS".equals(paymentResponse.getStatus())) {
            savedOrder.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            throw new RuntimeException("Payment failed for order " + savedOrder.getId());
        }

        // ----------- BƯỚC 6: Update trạng thái PAID -----------
        savedOrder.setStatus(OrderStatus.PAID);
        orderRepository.save(savedOrder);

        // ----------- BƯỚC 7: Trả về OrderResponse -----------
        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .customerName(customerResponse.getFullName())
                .orderDate(savedOrder.getOrderDate())
                .status(savedOrder.getStatus())
                .items(productInfos.stream().map(p -> {
                    int qty = orderRequest.getOrderItems().stream()
                            .filter(i -> i.getProductId().equals(p.getId()))
                            .findFirst().get().getQuantity();
                    return new OrderItemResponse(p.getId(), p.getName(),
                            qty, BigDecimal.valueOf(p.getPrice()),
                            BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(qty)));
                }).collect(Collectors.toList()))
                .totalAmount(totalAmount)
                .build();
    }
}
