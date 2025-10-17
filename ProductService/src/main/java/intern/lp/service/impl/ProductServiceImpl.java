package intern.lp.service.impl;


import intern.lp.dto.ProductRequest;
import intern.lp.dto.ProductResponse;
import intern.lp.entities.Product;
import intern.lp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import java.util.List;

@Service
public class ProductServiceImpl {
    @Autowired
    private  ProductRepository productRepository;


    // Lấy toàn bộ sản phẩm
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Lấy theo ID
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    // Tạo mới
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();
        return mapToResponse(productRepository.save(product));
    }

    // Cập nhật
    public ProductResponse update(Long id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());

        return mapToResponse(productRepository.save(existing));
    }

    // Xóa
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    // Mapping helper
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }
}

