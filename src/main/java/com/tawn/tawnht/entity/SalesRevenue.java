package com.tawn.tawnht.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "sales_revenue")
public class SalesRevenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    Seller seller;

    LocalDate salesDate;
    BigDecimal totalRevenue;
    BigDecimal totalOrders;
    BigDecimal averageOrderValue;
    Integer numberOfTransactions;
    Integer numberOfRefunds;
    BigDecimal refundAmount;
    BigDecimal netRevenue; // totalRevenue - refundAmount

    // Time period aggregation fields
    Integer year;
    Integer month;
    Integer dayOfMonth;
    Integer week;
    Integer quarter;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // Auto-populate time period fields from salesDate
        if (this.salesDate != null) {
            this.year = this.salesDate.getYear();
            this.month = this.salesDate.getMonthValue();
            this.dayOfMonth = this.salesDate.getDayOfMonth();
            this.week = this.salesDate.getDayOfYear() / 7 + 1;
            this.quarter = (this.salesDate.getMonthValue() - 1) / 3 + 1;
        }

        // Calculate net revenue
        if (this.totalRevenue != null && this.refundAmount != null) {
            this.netRevenue = this.totalRevenue.subtract(this.refundAmount);
        } else if (this.totalRevenue != null) {
            this.netRevenue = this.totalRevenue;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

        // Recalculate net revenue
        if (this.totalRevenue != null && this.refundAmount != null) {
            this.netRevenue = this.totalRevenue.subtract(this.refundAmount);
        } else if (this.totalRevenue != null) {
            this.netRevenue = this.totalRevenue;
        }
    }
}
