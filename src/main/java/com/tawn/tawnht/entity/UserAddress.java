package com.tawn.tawnht.entity;

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
@Table(name = "user_address")
public class UserAddress {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    User user;
    @OneToMany(mappedBy = "userAddress",fetch = FetchType.LAZY)
    Set<Order> orders;
    String city;
    String district;
    String commune;
    String status;
    String address;
    Boolean isDefault;
    String addressType;
    String phoneNumber;
    String name;
    LocalDateTime createdAt;
}
