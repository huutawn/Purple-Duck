package com.tawn.tawnht.dto.request;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreationRequest {
    Long categoryId;
    Long sellerId;
    String name;
    String description;
    String slug;
    String metaTitle;
    String metaDescription;
    List<String> images;
    String coverImage;
    String warrantyInfo;
    List<ProductVariantRequest> variants;
}
