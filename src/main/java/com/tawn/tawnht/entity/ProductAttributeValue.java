package com.tawn.tawnht.entity;

import java.util.Set;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product_attribute_values")
public class ProductAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String value;
    String displayValue;

    @ManyToOne
    ProductAttribute productAttribute;

    @OneToMany(
            mappedBy = "productAttributeValue",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    Set<ProductVariantAttribute> productVariantAttributes;
}
