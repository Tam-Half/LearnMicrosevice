package intern.lp.service.impl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import intern.lp.entities.Product;
import intern.lp.repository.ProductRepository;
import intern.lp.service.ProductService;
@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
	@Autowired
	private ProductRepository productRepository;

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = "product.queue", durable = "true"),
			exchange = @Exchange(value = "exchange_demo"),
			key = "product.queue"
	))
	public Object handleMessage(@Payload Map<String, Object> request ){
		log.info("✅ Received product request: {}", request);
		try{
			String action = request.get("action").toString();
			Long productId= null;
			Object productIddObj = request.get("productId");
			if (productIddObj instanceof Integer) {
				productId = Long.valueOf((Integer) productIddObj);
			} else if (productIddObj instanceof Long) {
				productId = (Long) productIddObj;
			} else if (productIddObj instanceof String) {
				productId = Long.parseLong((String) productIddObj);
			}

			if("GET_PRODUCT".equals(action)){
				Product product = productRepository.findById(productId).get();
				Map<String, Object> response = new HashMap<>();
				response.put("status", "SUCCESS");
				log.info("Product send {}" , product.toString());
				response.put("product", product);
				response.put("correlationId", request.get("correlationId"));
				log.info("📤 Sending success response: {}", response);

				return response;
			}

		}catch (Exception e){
			log.error("Error processing product message: {}", e.getMessage());

		}
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", "ERROR");
		errorResponse.put("message", "Invalid product request");
		return errorResponse;
	}

	@Override
	public List<Product> getAllProducts() {
		// TODO Auto-generated method stub

		return productRepository.findAll();
	}

	@Override
	public Optional<Product> getById(Long id) {
		return productRepository.findById(id);
	}

	@Override
	public Product create(Product p) {
		return productRepository.save(p);
	}

	@Override
	public Product updateProduct(Long id, Product p) {
		// TODO Auto-generated method stub
		Product pexit = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not Found By Id" + id));
		;

		return null;
	}

	@Override
	public void delete(Long id) {
		productRepository.deleteById(id);
	}

}
