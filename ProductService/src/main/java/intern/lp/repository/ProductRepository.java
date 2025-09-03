package intern.lp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import intern.lp.entities.Product;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

}
