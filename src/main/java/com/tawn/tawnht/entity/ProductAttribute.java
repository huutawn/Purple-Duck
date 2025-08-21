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
@Table(name = "product_attributes")
public class ProductAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    String displayName;
    String attributeType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "productAttribute", fetch = FetchType.LAZY)
    Set<ProductAttributeValue> productAttributeValue;
}
