package com.tawn.tawnht.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

public class RevenueAnalyticsResponse {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DailyRevenue {
        LocalDate date;
        BigDecimal revenue;
        BigDecimal netRevenue;
        Integer transactions;
        BigDecimal averageOrderValue;
        BigDecimal growthPercentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MonthlyRevenue {
        Integer year;
        Integer month;
        String monthName;
        BigDecimal revenue;
        BigDecimal netRevenue;
        Integer transactions;
        BigDecimal averageOrderValue;
        BigDecimal growthPercentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class YearlyRevenue {
        Integer year;
        BigDecimal revenue;
        BigDecimal netRevenue;
        Integer transactions;
        BigDecimal averageOrderValue;
        BigDecimal growthPercentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RevenueSummary {
        BigDecimal totalRevenue;
        BigDecimal netRevenue;
        BigDecimal totalRefunds;
        Integer totalTransactions;
        BigDecimal averageOrderValue;
        BigDecimal overallGrowthPercentage;
        LocalDate periodStart;
        LocalDate periodEnd;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DashboardMetrics {
        BigDecimal todayRevenue;
        BigDecimal monthRevenue;
        BigDecimal yearRevenue;
        BigDecimal totalRevenue;

        BigDecimal todayGrowth;
        BigDecimal monthGrowth;
        BigDecimal yearGrowth;

        Integer todayTransactions;
        Integer monthTransactions;
        Integer yearTransactions;
        Integer totalTransactions;

        BigDecimal averageOrderValue;
        BigDecimal topSellingProductRevenue;
        String topSellingProductName;

        List<DailyRevenue> last30Days;
        List<MonthlyRevenue> last12Months;
    }
}
