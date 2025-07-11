package com.tawn.tawnht.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "sellers")
public class Seller {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true,mappedBy = "seller")
    Set<Product> products;
    @OneToOne(mappedBy = "seller",cascade = CascadeType.ALL)
            SellerPolicy sellerPolicy;
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY,mappedBy = "seller")
            List<SubOrder> subOrders;
    String storeName;
    String storeDescription;
    String storeLogo;
    int rating;
    Boolean isVerified;
    String verifyToken;
    LocalDateTime createdAt;
}
