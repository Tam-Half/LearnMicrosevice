package intern.lp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private int namsx;
	private double price;
	
	
	public Product() {
		super();
	}
	public Product(long id, String name, int namsx, double price) {
		super();
		this.id = id;
		this.name = name;
		this.namsx = namsx;
		this.price = price;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNamsx() {
		return namsx;
	}
	public void setNamsx(int namsx) {
		this.namsx = namsx;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "Product [id=" + id + ", name=" + name + ", namsx=" + namsx + ", price=" + price + "]";
	}
	
}
