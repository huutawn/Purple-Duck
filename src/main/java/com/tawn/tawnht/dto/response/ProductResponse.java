package com.tawn.tawnht.dto.response;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    Long categoryId;
    String categoryName;
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
    boolean isActive;
    List<ProductVariantResponse> productVariants;
}
