package com.tawn.tawnht.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    String id;
    String firstName;
    String lastName;
    String email;
    String picture;

    // Order statistics
    Long totalOrders;
    BigDecimal totalSpent;
    BigDecimal averageOrderValue;
    String lastOrderStatus;
    LocalDateTime lastOrderDate;
    LocalDateTime firstOrderDate;

    // Contact information from most recent order
    String phone;
    String city;
    String address;

    // Customer status
    Boolean isActive; // Has orders in last 6 months
    String customerTier; // VIP, Regular, New based on spending
}
