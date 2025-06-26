package com.tawn.tawnht.dto.request;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

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
