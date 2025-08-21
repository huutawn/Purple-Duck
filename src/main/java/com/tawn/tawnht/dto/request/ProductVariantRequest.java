package com.tawn.tawnht.dto.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantRequest {
    Long productId;
    String sku;
    BigDecimal price;
    Integer stock;
    String image;
    List<AttributeRequest> attributes;
}
