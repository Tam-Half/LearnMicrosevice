package intern.lp.service.impl;

import intern.lp.dto.request.*;
import intern.lp.dto.response.*;
import intern.lp.entites.Order;
import intern.lp.entites.OrderItem;
import intern.lp.enums.OrderStatus;
import intern.lp.events.PaymentSuccessEvent;
import intern.lp.repository.OrderRepository;
import intern.lp.service.CustomerService;
import intern.lp.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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

    // Exchange Inventory
    private static final String INVENTORY_EXCHANGE = "inventory-exchange";
    private static final String INVENTORY_ROUTING_KEY = "inventory.check";

    // Exchange Payment
    private static final String PAYMENT_EXCHANGE = "payment-exchange";
    private static final String PAYMENT_ROUTING_KEY = "payment.create";

    // Exchange Shipping
    private static final String SHIPPING_EXCHANGE = "order-exchange";
    private static final String SHIPPING_ROUTING_KEY = "order.payment.completed";

    private static final String NOTIFICATION_EXCHANGE = "notification-exchange";
    private static final String NOTIFICATION_ROUTING_KEY = "notification.send";
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {

        // ----------- STEP 1: Get Products and Customer -----------
        List<ProductResponse> productInfos = orderRequest.getOrderItems().stream()
                .map(item -> productClient.getProductById(item.getProductId()))
                .collect(Collectors.toList());

        CustomerResponse customerResponse = customerClient.getCustomerById(orderRequest.getCustomerId());
        if (customerResponse == null) {
            throw new RuntimeException("Customer not found with id: " + orderRequest.getCustomerId());
        }

        // ----------- STEP 2: Calculate Total Amount -----------
        BigDecimal totalAmount = productInfos.stream()
                .map(p -> {
                    int qty = orderRequest.getOrderItems().stream()
                            .filter(i -> i.getProductId().equals(p.getId()))
                            .findFirst().get().getQuantity();
                    return BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ----------- STEP 3: Check Inventory TRƯỚC KHI LƯU -----------
        InventoryRequest inventoryRequest = new InventoryRequest(null, orderRequest.getOrderItems());
        InventoryResponse inventoryResponse = (InventoryResponse) rabbitTemplate.convertSendAndReceive(
                INVENTORY_EXCHANGE, INVENTORY_ROUTING_KEY, inventoryRequest);

        // ✅ Kiểm tra inventory TRƯỚC, nếu không đủ thì throw exception NGAY
        if (inventoryResponse == null || !inventoryResponse.isAvailable()) {
            log.warn("Order creation failed: Not enough stock for requested items");
            throw new RuntimeException("Order failed: Not enough stock!");
        }

        // ----------- STEP 4: Save Order PENDING (CHỈ KHI INVENTORY ĐỦ) -----------
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

        // ----------- STEP 5: Send Payment Request -----------
        PaymentRequest paymentRequest = new PaymentRequest(savedOrder.getId(), totalAmount, "COD");
        rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_ROUTING_KEY, paymentRequest);

        log.info("Payment request sent for order {}", savedOrder.getId());

        // ----------- STEP 6: Return OrderResponse -----------
        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .customerName(customerResponse.getFullName())
                .orderDate(savedOrder.getOrderDate())
                .status(OrderStatus.PENDING)
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

    /**
     * ✅ LISTENER: Cập nhật trạng thái order sau khi payment hoàn tất
     */
    @Transactional
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "order.payment.completed", durable = "true"),
            exchange = @Exchange(name = "payment-exchange", type = "direct"),
            key = "payment.completed"
    ))
    public void handlePaymentEvent(PaymentSuccessEvent event) {
        log.info("Received payment event for order {}: status={}", event.getOrderId(), event.getStatus());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        // ✅ Kiểm tra idempotency (tránh xử lý trùng)
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("Order {} already PAID. Skipping event.", order.getId());
            return;
        }

        // ✅ THÊM: Bỏ qua nếu order không phải PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} is not PENDING (current status: {}). Skipping event.",
                    order.getId(), order.getStatus());
            return;
        }

        // ✅ Cập nhật trạng thái
        if ("SUCCESS".equals(event.getStatus())) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            log.info("Order {} updated to PAID", order.getId());

            sendToShippingService(order);

        } else {
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
            log.warn("Order {} CANCELED due to payment failure", order.getId());
        }
    }

    /**
     * ✅ GỬI THÔNG TIN ĐƠN HÀNG VÀ KHÁCH HÀNG CHO SHIPPING SERVICE
     */
    private void sendToShippingService(Order order) {
        try {
            log.info("Preparing shipping request for order {}", order.getId());

            // Lấy thông tin khách hàng
            CustomerResponse customer = customerClient.getCustomerById(order.getCustomerId());
            if (customer == null) {
                log.error("Customer not found for order {}. Cannot create shipping request.", order.getId());
                return;
            }

            // Lấy thông tin chi tiết sản phẩm
            List<ShippingItemDTO> shippingItems = order.getOrderItems().stream()
                    .map(item -> {
                        try {
                            ProductResponse product = productClient.getProductById(item.getProductId());
                            return new ShippingItemDTO(
                                    item.getProductId(),
                                    product.getName(),
                                    item.getQuantity(),
                                    item.getPrice()
                            );
                        } catch (Exception e) {
                            log.error("Failed to get product {} for order {}: {}",
                                    item.getProductId(), order.getId(), e.getMessage());
                            // Fallback: không có tên sản phẩm
                            return new ShippingItemDTO(
                                    item.getProductId(),
                                    "Unknown Product",
                                    item.getQuantity(),
                                    item.getPrice()
                            );
                        }
                    })
                    .collect(Collectors.toList());

            // Tạo ShippingRequest
            ShippingRequest shippingRequest = ShippingRequest.builder()
                    .orderId(order.getId())
                    .customerId(order.getCustomerId())
                    .customerName(customer.getFullName())
                    .customerPhone(customer.getPhone())
                    .customerEmail(customer.getEmail())
                    .shippingAddress(customer.getAddress())
                    .orderItems(shippingItems)
                    .totalAmount(order.getTotalAmount())
                    .orderDate(order.getOrderDate())
                    .build();

            // Gửi message đến Shipping Service
            rabbitTemplate.convertAndSend(SHIPPING_EXCHANGE, SHIPPING_ROUTING_KEY, shippingRequest);
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, NOTIFICATION_ROUTING_KEY, shippingRequest);

            log.info("✅ Shipping request sent successfully for order {}", order.getId());
            log.info("   Customer: {} - {}", customer.getFullName(), customer.getPhone());
            log.info("   Address: {}", customer.getAddress());
            log.info("   Items: {} products", shippingItems.size());
            log.info("   Total: {}", order.getTotalAmount());

        } catch (Exception e) {
            log.error("❌ Failed to send shipping request for order {}: {}", order.getId(), e.getMessage(), e);
        }
    }
}