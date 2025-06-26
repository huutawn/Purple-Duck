package com.tawn.tawnht.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private BigDecimal subTotal;
    private LocalDateTime createdAt;
}
