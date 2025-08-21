package com.tawn.tawnht.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    Long id;
    ProductVariantResponse productVariant;
    Integer quantity;
    BigDecimal price;
    BigDecimal subTotal; // quantity * price
}
