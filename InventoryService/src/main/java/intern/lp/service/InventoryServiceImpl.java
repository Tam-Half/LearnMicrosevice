package intern.lp.service;

import intern.lp.dto.InventoryRequest;
import intern.lp.dto.InventoryResponse;
import intern.lp.entites.Inventory;
import intern.lp.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class InventoryServiceImpl {
    @Autowired
    private InventoryRepository inventoryRepository;
    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public InventoryResponse getByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for productId: " + productId));
        return mapToResponse(inventory);
    }

    public InventoryResponse create(InventoryRequest request) {
        Inventory inventory = Inventory.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();
        return mapToResponse(inventoryRepository.save(inventory));
    }

    public InventoryResponse update(Long productId, InventoryRequest request) {
        Inventory existing = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for productId: " + productId));

        existing.setQuantity(request.getQuantity());
        return mapToResponse(inventoryRepository.save(existing));
    }

    public void delete(Long productId) {
        Inventory existing = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for productId: " + productId));
        inventoryRepository.delete(existing);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .build();
    }
}
