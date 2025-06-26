package com.tawn.tawnht.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String email;
    @JsonIgnore
    String password;
    String firstName;
    LocalDate dob;
    String lastName;
    String tokenVerify;
    Boolean isVerified;
    LocalDateTime timeCreateToken;

    @ManyToMany
    Set<Role> roles;
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "user",orphanRemoval = true,fetch = FetchType.LAZY)
    Set<UserAddress> userAddresses;
    @OneToOne
    Cart cart;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    Seller seller;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "user",orphanRemoval = true,fetch = FetchType.LAZY)
    Set<Order> orders;
}
