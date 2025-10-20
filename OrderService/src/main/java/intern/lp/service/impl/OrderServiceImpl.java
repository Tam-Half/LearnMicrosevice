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

        // ✅ 1. Gọi product-service để lấy thông tin sản phẩm
        List<ProductResponse> productInfos = orderRequest.getOrderItems().stream()
                .map(item -> {
                    ProductResponse product = productClient.getProductById(item.getProductId());
                    log.info("Call product-service success, product: {}", product);
                    return product;
                })
                .collect(Collectors.toList());

        log.info("orderRequest.getCustomerId() " + orderRequest.getCustomerId());
        // ✅ 2. Gọi customer-service lấy thông tin khách hàng
        CustomerResponse customerResponse = customerClient.getCustomerById(orderRequest.getCustomerId());

       log.info("CUSTOMER " + customerResponse);

        // ✅ 3. Lưu đơn hàng trước (PENDING) -> tạo orderId để gửi qua Inventory
        Order order = new Order();
        order.setCustomerId(orderRequest.getCustomerId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(orderRequest.getOrderItems());
        Order savedOrder = orderRepository.save(order);
        log.info("Order saved temporarily with ID = {} (status PENDING)", savedOrder.getId());

        // ✅ 4. Gửi yêu cầu Inventory kèm orderId để kiểm tra tồn kho
        InventoryRequest inventoryRequest = new InventoryRequest();
        inventoryRequest.setOrderId(savedOrder.getId());
        inventoryRequest.setItems(orderRequest.getOrderItems());

        log.info("Sending inventory check for order {}...", savedOrder.getId());
        InventoryResponse inventoryResponse = (InventoryResponse) rabbitTemplate.convertSendAndReceive(
                INVENTORY_EXCHANGE,
                INVENTORY_ROUTING_KEY,
                inventoryRequest
        );

        if (inventoryResponse == null) {
            throw new RuntimeException("Inventory Service Timeout!");
        }

        // ✅ 5. Nếu không đủ hàng => hủy đơn
        if (!inventoryResponse.isAvailable()) {
            savedOrder.setStatus(OrderStatus.CANCELED);
            orderRepository.save(savedOrder);
            throw new RuntimeException("Order canceled: Inventory not enough for products: "
                    + inventoryResponse.getUnavailableItems());
        }

        // ✅ 6. Nếu đủ hàng => xác nhận đơn
        savedOrder.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(savedOrder);
        log.info("Order {} confirmed successfully!", savedOrder.getId());

        // ✅ 7. Trả về thông tin order đầy đủ
        List<OrderItemResponse> itemResponses = savedOrder.getOrderItems().stream().map(item -> {
            ProductResponse product = productInfos.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst()
                    .orElseThrow();
            BigDecimal total = BigDecimal.valueOf(product.getPrice() * item.getQuantity());
            return OrderItemResponse.builder()
                    .productId(item.getProductId())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .price(BigDecimal.valueOf(product.getPrice()))
                    .total(total)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .map(OrderItemResponse::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .customerName(customerResponse.getFullName())
                .orderDate(savedOrder.getOrderDate())
                .status(savedOrder.getStatus())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .build();
    }
}
