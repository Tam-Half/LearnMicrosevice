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

import intern.lp.entites.Customer;
import intern.lp.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
	@Autowired
	private CustomerService customerService;
	
	@GetMapping
	public List<Customer> getAllCustomer(){
		return customerService.getAll();
	}
	
	@GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
		  return customerService.getById(id)
		            .map(ResponseEntity::ok)
		            .orElse(ResponseEntity.notFound().build());
	}
	
	@PostMapping
	public Customer createCustomer (@RequestBody Customer customer) {
		return customerService.create(customer);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer){
		return ResponseEntity.ok(customerService.updateCustomer(id, customer));
	}
	
}
