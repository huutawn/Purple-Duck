package com.tawn.tawnht.dto.response;

import com.tawn.tawnht.entity.Cart;
import com.tawn.tawnht.entity.CartItem;
import com.tawn.tawnht.entity.ProductVariant;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyJoinColumn;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    private Long id; // ID của CartItem
    private LocalDateTime addedAt; // Thời gian thêm vào giỏ
    private List<VariantQuantity> variants; // Danh sách biến thể và số lượng

    // DTO con để ánh xạ Map<ProductVariant, Integer>
    @Data
    public static class VariantQuantity {
        private Long variantId; // ID của ProductVariant
        private String productName; // Tên sản phẩm từ Product
        private BigDecimal price; // Giá của biến thể
        private Integer stock; // Số lượng tồn kho
        private String image; // Link ảnh
        private List<AttributeResponse> attributes; // Thuộc tính của biến thể
        private Integer quantity; // Số lượng trong giỏ
    }
}
