package intern.lp.service;

import java.util.List;

import intern.lp.dto.OrderRequest;
import intern.lp.entites.Order;

public interface OrderService {
	List<Order> getAllOrder ();
	
    String createOrder(OrderRequest orderRequest);

    Order getOrderById(Long id);
}
