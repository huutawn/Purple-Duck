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
public class NewOrderMessage {
    Long subOrderId;
    Long orderId;
    String customerName;
    String customerEmail;
    String status;
    BigDecimal subTotal;
    List<OrderItemResponse> orderItems;
    UserAddressResponse address;
    LocalDateTime createdAt;
    String paymentMethod;
    String notes;
}
