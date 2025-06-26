package com.tawn.tawnht.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "products")
public class Product {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    Category category;
    @ManyToOne
    @JsonBackReference
    Seller seller;
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true,mappedBy = "product")
    Set<ProductVariant> productVariants;
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.LAZY,mappedBy = "product")
    Set<ProductImage> images;
    String name;
    String description;
    String coverImage;
    String slug;
    Integer purchase;
    String metaTitle;
    String metaDescription;
    boolean isActive;
    String warrantyInfo;
    LocalDateTime createdAt;
}
