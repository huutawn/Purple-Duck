package com.tawn.tawnht.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    Long id;
    Long productId;
    String productName;
    String sku;
    BigDecimal price;
    Integer stock;
    String image;
    List<AttributeResponse> attributes;
}
