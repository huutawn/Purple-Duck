package com.tawn.tawnht.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
        private String productName;
        private Long productId; // Tên sản phẩm từ Product
        private BigDecimal price; // Giá của biến thể
        private Integer stock; // Số lượng tồn kho
        private String image; // Link ảnh
        private List<AttributeResponse> attributes; // Thuộc tính của biến thể
        private Integer quantity; // Số lượng trong giỏ
    }
}
