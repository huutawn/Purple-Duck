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
public class ProductImageResponse {
    Long id;
    Long productId;
    Long variantId;
    String imageUrl;
    Integer displayOrder;
    LocalDateTime createdAt;
}
