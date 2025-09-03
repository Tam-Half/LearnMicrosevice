package intern.lp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import intern.lp.entities.Product;
import intern.lp.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	
	@Autowired
	private ProductService productService;
	
	@GetMapping
	public List<Product> getAllProducts(){
		return productService.getAllProducts();
	}
	
	@GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
		  return productService.getById(id)
		            .map(ResponseEntity::ok)
		            .orElse(ResponseEntity.notFound().build());
	}
	
	@PostMapping
	public Product createProduct (@RequestBody Product p) {
		return productService.create(p);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product p){
		return ResponseEntity.ok(productService.updateProduct(id, p));
	}

}
