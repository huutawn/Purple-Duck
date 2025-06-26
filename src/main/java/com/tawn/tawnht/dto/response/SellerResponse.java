package com.tawn.tawnht.dto.response;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerResponse {
    Long id;
    Long userId;
    String storeName;
    String storeDescription;
    String storeLogo;
    int rating;
    Boolean isVerified;
    LocalDateTime createdAt;
}
