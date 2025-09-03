package intern.lp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import intern.lp.entites.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{
}
