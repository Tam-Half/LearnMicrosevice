package intern.lp.service.impl;

import intern.lp.entites.OrderItem;
import intern.lp.repository.OrderItemRepository;
import intern.lp.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository repository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public Optional<OrderItem> getOrderItemById(Long id) {
        return orderItemRepository.findById(id);
    }
}
