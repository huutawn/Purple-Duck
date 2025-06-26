package com.tawn.tawnht.dto.response;

import com.tawn.tawnht.entity.Order;
import com.tawn.tawnht.entity.ProductVariant;
import com.tawn.tawnht.entity.SubOrder;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

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
