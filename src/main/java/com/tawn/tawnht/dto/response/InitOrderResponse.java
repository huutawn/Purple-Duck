package com.tawn.tawnht.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitOrderResponse {
    String userId;
    String userName;
    BigDecimal totalAmount;
    BigDecimal shippingFee;
    BigDecimal taxAmount;
    BigDecimal discountAmount;
    String couponCode;
    String status;
    String paymentMethod;
    UserAddressResponse userAddress;
    List<SubOrderResponse> subOrders;
    String trackingNumber;
    String shippingCarrier;
    LocalDate estimatedDeliveryDate;
    String note;
    LocalDateTime createdAt;
}
