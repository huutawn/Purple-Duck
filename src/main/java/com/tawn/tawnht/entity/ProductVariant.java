package com.tawn.tawnht.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Product product;
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "productVariant")
    Set<ProductVariantAttribute> productVariantAttributes;
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY,mappedBy = "productVariant")
            Set<OrderItem> orderItems;
    String sku;
    String image;
    BigDecimal price;
    Integer stock;
}
