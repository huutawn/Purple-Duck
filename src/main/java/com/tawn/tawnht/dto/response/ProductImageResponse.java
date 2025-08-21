package com.tawn.tawnht.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
