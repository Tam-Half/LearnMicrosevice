package intern.lp.service;

import intern.lp.dto.request.InventoryRequest;
import intern.lp.dto.response.InventoryResponse;
import intern.lp.entites.Inventory;
import intern.lp.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static intern.lp.config.RabbitMQConfig.INVENTORY_QUEUE;

@Service
@Slf4j
public class InventoryServiceImpl {

    @Autowired
    private InventoryRepository inventoryRepository;

     @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "inventory_check", durable = "true"),
            exchange = @Exchange(name = "inventory-exchange", type = "direct"),
            key = "inventory.check"
    ))
    public InventoryResponse checkInventory(InventoryRequest request) {

        log.info("📦 Received inventory check for Order ID: {}", request.getOrderId());

        List<Long> unavailableProducts = new ArrayList<>();

        // ✅ Kiểm tra tồn kho
        request.getItems().forEach(item -> {
            Inventory inv = inventoryRepository.findByProductId(item.getProductId());

            if (inv == null || inv.getQuantity() < item.getQuantity()) {
                unavailableProducts.add(item.getProductId());
            }
        });

        // ❌ Nếu thiếu hàng → báo thiếu luôn
        if (!unavailableProducts.isEmpty()) {
            log.warn("❌ Not enough stock for products: {}", unavailableProducts);
            return InventoryResponse.builder()
                    .orderId(request.getOrderId())
                    .available(false)
                    .unavailableItems(unavailableProducts)
                    .build();
        }

        // ✅ Nếu đủ hàng → TRỪ TỒN KHO
        request.getItems().forEach(item -> {
            Inventory inv = inventoryRepository.findByProductId(item.getProductId());
            inv.setQuantity(inv.getQuantity() - item.getQuantity());
            inventoryRepository.save(inv);
        });

        log.info("✅ Inventory OK for order {}", request.getOrderId());

        return InventoryResponse.builder()
                .orderId(request.getOrderId())
                .available(true)
                .unavailableItems(new ArrayList<>())
                .build();
    }
}
