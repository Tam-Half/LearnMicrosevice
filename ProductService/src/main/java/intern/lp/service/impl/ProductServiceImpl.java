package intern.lp.service.impl;


import intern.lp.dto.ProductRequest;
import intern.lp.dto.ProductResponse;
import intern.lp.entities.Product;
import intern.lp.mapper.ProductMapper;
import intern.lp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ProductServiceImpl {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Cacheable(value = "products-list", key = "'all'")
    public List<ProductResponse> getAll() {
        System.out.println("⏳ Querying DB for ALL products...");
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }


    @Cacheable(value = "product", key = "#id")
    public ProductResponse getById(Long id) {
        System.out.println("⏳ Querying DB... Product By Id " + id);
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @CachePut(value = "product", key = "#result.id")
    @CacheEvict(value = "products-list", allEntries = true)
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        return productMapper.toResponse(productRepository.save(product));
    }

    @CachePut(value = "product", key = "#id")
    @CacheEvict(value = "products-list", allEntries = true)
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        productMapper.updateEntityFromRequest(request, product);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products-list", allEntries = true)
    })
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}

