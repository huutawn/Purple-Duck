package com.tawn.tawnht.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product_variant_attributes")
public class ProductVariantAttribute {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    ProductVariant productVariant;
    @ManyToOne
    @JoinColumn(name = "product_attribute_value_id")
    ProductAttributeValue productAttributeValue;

}
