package com.tawn.tawnht.dto.response;

import com.tawn.tawnht.dto.request.AttributeValueRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

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
