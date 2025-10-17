package intern.lp.controller;

import intern.lp.dto.InventoryRequest;
import intern.lp.dto.InventoryResponse;
import intern.lp.service.InventoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    @Autowired
    private InventoryServiceImpl inventoryService;


    @GetMapping
    public List<InventoryResponse> getAll() {
        return inventoryService.getAll();
    }

    @GetMapping("/{productId}")
    public InventoryResponse getByProductId(@PathVariable Long productId) {
        return inventoryService.getByProductId(productId);
    }

    @PostMapping
    public InventoryResponse create(@RequestBody InventoryRequest request) {
        return inventoryService.create(request);
    }

    @PutMapping("/{productId}")
    public InventoryResponse update(@PathVariable Long productId, @RequestBody InventoryRequest request) {
        return inventoryService.update(productId, request);
    }

    @DeleteMapping("/{productId}")
    public void delete(@PathVariable Long productId) {
        inventoryService.delete(productId);
    }
}