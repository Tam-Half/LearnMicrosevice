package intern.lp.mapper;

import intern.lp.dto.ProductRequest;
import intern.lp.dto.ProductResponse;
import intern.lp.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

// Báo cho MapStruct đây là mapper và tạo Spring Bean
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Tự động map Entity (Product) sang DTO (ProductResponse)
     * Tên trường giống nhau sẽ được tự động map.
     */
    ProductResponse toResponse(Product product);

    /**
     * Tự động map DTO (ProductRequest) sang Entity (Product)
     */
    Product toEntity(ProductRequest request);

    /**
     * Cập nhật một 'Product' (target) có sẵn từ 'ProductRequest' (source)
     * Dùng cho hàm update()
     */
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}