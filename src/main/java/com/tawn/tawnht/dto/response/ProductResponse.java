package com.tawn.tawnht.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    Long categoryId;
    Long sellerId;
    String name;
    String description;
    String slug;
    String coverImage;
    List<ProductImageResponse> images;
    String metaTitle;
    Integer purchase;
    String metaDescription;
    String warrantyInfo;
    LocalDateTime createdAt;
    boolean isActive;
    List<ProductVariantResponse> productVariants;


}
