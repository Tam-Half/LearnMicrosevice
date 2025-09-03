package intern.lp.service;

import java.util.List;
import java.util.Optional;

import intern.lp.entites.Customer;

public interface CustomerService {
	List<Customer> getAll();

	Optional<Customer> getById(Long id);

	Customer create(Customer customer);

	Customer updateCustomer(Long id, Customer cus);

	void delete(long id);
}
