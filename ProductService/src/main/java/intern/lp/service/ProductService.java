package intern.lp.service;

import java.util.List;
import java.util.Optional;
import intern.lp.entities.Product;

public interface ProductService {
	
	List<Product> getAllProducts();

	Optional<Product> getById(Long id);

	Product create(Product p);

	Product updateProduct(Long id, Product p);

	void delete(Long id);

}
