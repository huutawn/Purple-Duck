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
@Table(name = "user_address")
public class UserAddress {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    User user;
    @OneToOne(mappedBy = "userAddress",fetch = FetchType.LAZY)
            Order order;
    String city;
    String district;
    String commune;
    String address;
    Boolean isDefault;
    String addressType;
    LocalDateTime createdAt;
}
