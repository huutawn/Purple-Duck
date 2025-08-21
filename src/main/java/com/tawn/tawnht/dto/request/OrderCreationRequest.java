package com.tawn.tawnht.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreationRequest {
    @NotEmpty(message = "Items list cannot be empty")
    private List<OrderItemRequest> items;

    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    private String paymentMethod;

    private String couponCode;

    private String notes;
    boolean fromCart;
}
