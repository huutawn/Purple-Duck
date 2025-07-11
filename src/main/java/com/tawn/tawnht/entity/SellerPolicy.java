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
@Table(name = "seller_policy")
public class SellerPolicy {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @OneToOne
    Seller seller;
    String policyType;
    @Column(columnDefinition = "TEXT")
    String content;
    LocalDateTime createdAt;
}
