package com.tawn.tawnht.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "orders")
public class Order {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    User user;
    BigDecimal totalAmount;
    BigDecimal shippingFee;
    BigDecimal taxAmount;
    BigDecimal discountAmount;
    String couponCode;
    String status;
    String paymentMethod;
    @OneToOne
    UserAddress userAddress;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,fetch = FetchType.LAZY,orphanRemoval = true)
    List<SubOrder> subOrders;
    String trackingNumber;
    String shippingCarrier;
    LocalDate estimatedDeliveryDate;
    @Column(columnDefinition = "TEXT")
    String note;
    LocalDateTime createdAt;
}
