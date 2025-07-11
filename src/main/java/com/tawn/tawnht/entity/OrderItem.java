package com.tawn.tawnht.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    ProductVariant productVariant;
    @ManyToOne
            SubOrder subOrder;
    Integer quantity;
    BigDecimal price;
}
