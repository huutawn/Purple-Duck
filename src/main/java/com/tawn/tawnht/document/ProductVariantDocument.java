package com.tawn.tawnht.document;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDocument {
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl; // Image của variant
    // Có thể thêm các thuộc tính của variant dưới dạng Map hoặc List<String>
    // Ví dụ: Map<String, String> attributes (e.g., {"Color": "Red", "Size": "M"})
    @Field(type = FieldType.Nested, includeInParent = true)
    private List<ProductAttributeDocument> attributes;
}
