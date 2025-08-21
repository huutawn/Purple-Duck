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
public class SubOrderResponse {
    private Long subOrderId;
    private Long orderId;
    private String userName;
    private String status;
    private List<OrderItemResponse> orderItems;
    private UserAddressResponse address;
    private BigDecimal subTotal;
    private LocalDateTime createdAt;
}
