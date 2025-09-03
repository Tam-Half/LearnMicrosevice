package intern.lp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import intern.lp.entites.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
