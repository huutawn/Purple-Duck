package com.tawn.tawnht.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    Cart cart;
    // Map lưu ProductVariant và quantity
    @ElementCollection
    @MapKeyJoinColumn(name = "product_variant_id")
    @Column(name = "quantity")
    private Map<ProductVariant, Integer> variantQuantities = new HashMap<>();
    LocalDateTime addedAt;
}
