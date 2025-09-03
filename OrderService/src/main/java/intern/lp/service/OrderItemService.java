package intern.lp.service;

import intern.lp.entites.OrderItem;

import java.util.Optional;

public interface OrderItemService {
    Optional<OrderItem> getOrderItemById(Long id);
}
