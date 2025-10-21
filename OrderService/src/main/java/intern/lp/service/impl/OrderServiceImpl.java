package intern.lp.service.impl;

import intern.lp.dto.request.InventoryRequest;
import intern.lp.dto.response.*;
import intern.lp.dto.request.OrderRequest;
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

    private static final String INVENTORY_EXCHANGE = "inventory-exchange";
    private static final String INVENTORY_ROUTING_KEY = "inventory.check";

    public OrderResponse createOrder(OrderRequest orderRequest) {

        // ✅ 1. Gọi product-service lấy thông tin sản phẩm
        List<ProductResponse> productInfos = orderRequest.getOrderItems().stream()
                .map(item -> {
                    ProductResponse product = productClient.getProductById(item.getProductId());
                    log.info("Fetched product: {}", product);
                    return product;
                })
                .collect(Collectors.toList());

        // ✅ 2. Gọi customer-service
        CustomerResponse customerResponse = customerClient.getCustomerById(orderRequest.getCustomerId());
        if (customerResponse == null || customerResponse.getFullName() == null) {
            throw new RuntimeException("Customer not found with id: " + orderRequest.getCustomerId());
        }

        // ✅ 3. Tính totalAmount trước
        BigDecimal totalAmount = productInfos.stream()
                .map(p -> {
                    int qty = orderRequest.getOrderItems().stream()
                            .filter(i -> i.getProductId().equals(p.getId()))
                            .findFirst()
                            .get().getQuantity();
                    return BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ 4. Lưu order ở trạng thái PENDING
        Order order = new Order();
        order.setCustomerId(orderRequest.getCustomerId());

        List<OrderItem> orderItems = orderRequest.getOrderItems().stream().map(item -> {
            ProductResponse product = productInfos.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity()))); // ✅ tính giá

            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        log.info("Order {} saved as PENDING", savedOrder.getId());

        // ✅ 5. Kiểm tra tồn kho
        InventoryRequest inventoryRequest = new InventoryRequest();
        inventoryRequest.setOrderId(savedOrder.getId());
        inventoryRequest.setItems(orderRequest.getOrderItems());

        InventoryResponse inventoryResponse = (InventoryResponse) rabbitTemplate.convertSendAndReceive(
                INVENTORY_EXCHANGE,
                INVENTORY_ROUTING_KEY,
                inventoryRequest
        );

        if (inventoryResponse == null || !inventoryResponse.isAvailable()) {
            log.warn("Inventory not enough for order {}", savedOrder.getId());
            orderRepository.deleteById(savedOrder.getId()); // ❗XÓA KHỎI DB
            throw new RuntimeException("Order failed: Not enough stock!");
        }

        // ✅ 6. Xác nhận đơn hàng
        savedOrder.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(savedOrder);

        // ✅ 7. Trả về response
        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .customerName(customerResponse.getFullName())
                .orderDate(savedOrder.getOrderDate())
                .status(savedOrder.getStatus())
                .items(
                        productInfos.stream().map(p -> {
                            int qty = orderRequest.getOrderItems().stream()
                                    .filter(i -> i.getProductId().equals(p.getId()))
                                    .findFirst()
                                    .get().getQuantity();
                            return OrderItemResponse.builder()
                                    .productId(p.getId())
                                    .productName(p.getName())
                                    .quantity(qty)
                                    .price(BigDecimal.valueOf(p.getPrice()))
                                    .total(BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(qty)))
                                    .build();
                        }).collect(Collectors.toList())
                )
                .totalAmount(totalAmount)
                .build();
    }
}
