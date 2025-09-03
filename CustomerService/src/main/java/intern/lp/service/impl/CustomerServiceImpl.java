package intern.lp.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import intern.lp.entites.Customer;
import intern.lp.repository.CustomerRepository;
import intern.lp.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {
	@Autowired
	private CustomerRepository customerRepository;

	@Override
	public List<Customer> getAll() {
		return customerRepository.findAll();
	}

	@Override
	public Optional<Customer> getById(Long id) {
	    return customerRepository.findById(id);
	}

	@Override
	public Customer create(Customer customer) {
		return customerRepository.save(customer);
	}

	@Override
	public  Customer updateCustomer(Long id, Customer cus) {
		Customer cusold = customerRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Customer not Found By Id" + id));
		try {
			cusold.setName(cus.getName());
			cusold.setPhone(cus.getPhone());
			cusold.setEmail(cus.getEmail());
			cusold.setAddress(cus.getAddress());
			
		} catch (Exception err) {
			System.out.println(err);
		}
		return customerRepository.save(cusold);
	}

	@Override
	public void delete(long id) {
		customerRepository.deleteById(id);
	}

}
