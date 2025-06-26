package com.tawn.tawnht.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotEmpty(message = "Attribute value IDs cannot be empty")
    private List<Long> attributeValueIds;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
