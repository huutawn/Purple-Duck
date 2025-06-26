package com.tawn.tawnht.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

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

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true,mappedBy = "productAttribute",fetch = FetchType.LAZY)
            @JsonManagedReference
    Set<ProductAttributeValue> productAttributeValue;
}
