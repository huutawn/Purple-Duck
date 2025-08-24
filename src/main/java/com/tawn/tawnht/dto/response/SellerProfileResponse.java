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
public class SellerProfileResponse {
    // Seller information
    Long sellerId;
    String storeName;
    String storeDescription;
    String storeLogo;
    Integer rating;
    Boolean isVerified;
    LocalDateTime sellerCreatedAt;

    // User information
    String userId;
    String firstName;
    String lastName;
    String email;
    String picture;

    // Dashboard statistics
    Long totalOrders;
    Long pendingOrders;
    Long completedOrders;
    BigDecimal totalRevenue;
    BigDecimal monthlyRevenue;
    Long totalProducts;
    Long activeProducts;
    Long totalCustomers;
    Integer unreadNotifications;
}
