package com.tawn.tawnht.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddToCartReq {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotEmpty(message = "Attribute value IDs cannot be empty")
    private List<Long> attributeValueIds;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
