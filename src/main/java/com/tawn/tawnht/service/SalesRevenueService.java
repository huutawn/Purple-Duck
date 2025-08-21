package com.tawn.tawnht.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tawn.tawnht.dto.response.RevenueAnalyticsResponse;
import com.tawn.tawnht.entity.SalesRevenue;
import com.tawn.tawnht.entity.Seller;
import com.tawn.tawnht.repository.SalesRevenueRepository;
import com.tawn.tawnht.repository.jpa.SellerRepository;
import com.tawn.tawnht.repository.jpa.SubOrderRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SalesRevenueService {

    SalesRevenueRepository salesRevenueRepository;
    SellerRepository sellerRepository;
    SubOrderRepository subOrderRepository;

    /**
     * Update or create daily sales revenue for a seller
     */
    @Transactional
    public SalesRevenue updateDailySalesRevenue(Long sellerId, LocalDate date) {
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new RuntimeException("Seller not found"));

        // Calculate daily metrics from actual orders
        DailyMetrics metrics = calculateDailyMetrics(sellerId, date);

        // Find existing record or create new one
        Optional<SalesRevenue> existingRecord = salesRevenueRepository.findBySellerAndSalesDate(seller, date);

        SalesRevenue salesRevenue;
        if (existingRecord.isPresent()) {
            salesRevenue = existingRecord.get();
        } else {
            salesRevenue = SalesRevenue.builder().seller(seller).salesDate(date).build();
        }

        // Update metrics
        salesRevenue.setTotalRevenue(metrics.totalRevenue);
        salesRevenue.setNetRevenue(metrics.netRevenue);
        salesRevenue.setNumberOfTransactions(metrics.transactions);
        salesRevenue.setAverageOrderValue(metrics.averageOrderValue);
        salesRevenue.setNumberOfRefunds(metrics.refunds);
        salesRevenue.setRefundAmount(metrics.refundAmount);

        return salesRevenueRepository.save(salesRevenue);
    }

    /**
     * Get daily revenue data for a date range
     */
    @Transactional(readOnly = true)
    public List<RevenueAnalyticsResponse.DailyRevenue> getDailyRevenue(
            Long sellerId, LocalDate startDate, LocalDate endDate) {
        List<SalesRevenue> records = salesRevenueRepository.findDailyRevenueBySeller(sellerId, startDate, endDate);

        List<RevenueAnalyticsResponse.DailyRevenue> result = new ArrayList<>();

        for (SalesRevenue record : records) {
            BigDecimal growthPercentage =
                    calculateDailyGrowthPercentage(sellerId, record.getSalesDate(), record.getTotalRevenue());

            result.add(RevenueAnalyticsResponse.DailyRevenue.builder()
                    .date(record.getSalesDate())
                    .revenue(record.getTotalRevenue())
                    .netRevenue(record.getNetRevenue())
                    .transactions(record.getNumberOfTransactions())
                    .averageOrderValue(record.getAverageOrderValue())
                    .growthPercentage(growthPercentage)
                    .build());
        }

        return result;
    }

    /**
     * Get paginated daily revenue data
     */
    @Transactional(readOnly = true)
    public Page<RevenueAnalyticsResponse.DailyRevenue> getDailyRevenue(
            Long sellerId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<SalesRevenue> records =
                salesRevenueRepository.findDailyRevenueBySeller(sellerId, startDate, endDate, pageable);

        return records.map(record -> {
            BigDecimal growthPercentage =
                    calculateDailyGrowthPercentage(sellerId, record.getSalesDate(), record.getTotalRevenue());

            return RevenueAnalyticsResponse.DailyRevenue.builder()
                    .date(record.getSalesDate())
                    .revenue(record.getTotalRevenue())
                    .netRevenue(record.getNetRevenue())
                    .transactions(record.getNumberOfTransactions())
                    .averageOrderValue(record.getAverageOrderValue())
                    .growthPercentage(growthPercentage)
                    .build();
        });
    }

    /**
     * Get monthly revenue data for a specific year
     */
    @Transactional(readOnly = true)
    public List<RevenueAnalyticsResponse.MonthlyRevenue> getMonthlyRevenue(Long sellerId, Integer year) {
        List<Object[]> rawData = salesRevenueRepository.findMonthlyRevenueDataBySeller(sellerId, year);

        List<RevenueAnalyticsResponse.MonthlyRevenue> monthlyData = new ArrayList<>();

        for (Object[] row : rawData) {
            Integer rowYear = (Integer) row[0];
            Integer month = (Integer) row[1];
            String monthName = (String) row[2];
            BigDecimal revenue = (BigDecimal) row[3];
            BigDecimal netRevenue = (BigDecimal) row[4];
            Integer transactions = ((Number) row[5]).intValue();
            BigDecimal avgOrderValue = (BigDecimal) row[6];

            BigDecimal growth = calculateMonthlyGrowthPercentage(sellerId, rowYear, month, revenue);

            RevenueAnalyticsResponse.MonthlyRevenue monthlyRevenue = RevenueAnalyticsResponse.MonthlyRevenue.builder()
                    .year(rowYear)
                    .month(month)
                    .monthName(monthName)
                    .revenue(revenue)
                    .netRevenue(netRevenue)
                    .transactions(transactions)
                    .averageOrderValue(avgOrderValue)
                    .growthPercentage(growth)
                    .build();

            monthlyData.add(monthlyRevenue);
        }

        return monthlyData;
    }

    /**
     * Get yearly revenue data
     */
    @Transactional(readOnly = true)
    public List<RevenueAnalyticsResponse.YearlyRevenue> getYearlyRevenue(Long sellerId) {
        List<Object[]> rawData = salesRevenueRepository.findYearlyRevenueDataBySeller(sellerId);

        List<RevenueAnalyticsResponse.YearlyRevenue> yearlyData = new ArrayList<>();

        for (Object[] row : rawData) {
            Integer year = (Integer) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            BigDecimal netRevenue = (BigDecimal) row[2];
            Integer transactions = ((Number) row[3]).intValue();
            BigDecimal avgOrderValue = (BigDecimal) row[4];

            BigDecimal growth = calculateYearlyGrowthPercentage(sellerId, year, revenue);

            RevenueAnalyticsResponse.YearlyRevenue yearlyRevenue = RevenueAnalyticsResponse.YearlyRevenue.builder()
                    .year(year)
                    .revenue(revenue)
                    .netRevenue(netRevenue)
                    .transactions(transactions)
                    .averageOrderValue(avgOrderValue)
                    .growthPercentage(growth)
                    .build();

            yearlyData.add(yearlyRevenue);
        }

        return yearlyData;
    }

    /**
     * Get comprehensive dashboard metrics
     */
    @Transactional(readOnly = true)
    public RevenueAnalyticsResponse.DashboardMetrics getDashboardMetrics(Long sellerId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate thisMonth = today.withDayOfMonth(1);
        LocalDate lastMonth = thisMonth.minusMonths(1);
        LocalDate thisYear = today.withDayOfYear(1);
        LocalDate lastYear = thisYear.minusYears(1);
        LocalDate last30Days = today.minusDays(30);

        // Today's metrics
        BigDecimal todayRevenue = salesRevenueRepository.findTotalRevenueBySellerAndDate(sellerId, today);
        BigDecimal yesterdayRevenue = salesRevenueRepository.findTotalRevenueBySellerAndDate(sellerId, yesterday);
        Integer todayTransactions = salesRevenueRepository.findTotalTransactionsBySellerAndDate(sellerId, today);

        // Month's metrics
        BigDecimal monthRevenue = salesRevenueRepository.findTotalRevenueBySellerAndMonth(
                sellerId, today.getYear(), today.getMonthValue());
        BigDecimal lastMonthRevenue = salesRevenueRepository.findTotalRevenueBySellerAndMonth(
                sellerId, lastMonth.getYear(), lastMonth.getMonthValue());
        Integer monthTransactions = salesRevenueRepository.findTotalTransactionsBySellerAndMonth(
                sellerId, today.getYear(), today.getMonthValue());

        // Year's metrics
        BigDecimal yearRevenue = salesRevenueRepository.findTotalRevenueBySellerAndYear(sellerId, today.getYear());
        BigDecimal lastYearRevenue =
                salesRevenueRepository.findTotalRevenueBySellerAndYear(sellerId, lastYear.getYear());
        Integer yearTransactions =
                salesRevenueRepository.findTotalTransactionsBySellerAndYear(sellerId, today.getYear());

        // Total metrics
        BigDecimal totalRevenue = salesRevenueRepository.findTotalRevenueBySeller(sellerId);
        BigDecimal averageOrderValue = salesRevenueRepository.findAverageOrderValueBySeller(sellerId);

        // Growth calculations
        BigDecimal todayGrowth = calculateGrowthPercentage(todayRevenue, yesterdayRevenue);
        BigDecimal monthGrowth = calculateGrowthPercentage(monthRevenue, lastMonthRevenue);
        BigDecimal yearGrowth = calculateGrowthPercentage(yearRevenue, lastYearRevenue);

        // Recent data for charts
        List<RevenueAnalyticsResponse.DailyRevenue> last30DaysData = getDailyRevenue(sellerId, last30Days, today);
        List<RevenueAnalyticsResponse.MonthlyRevenue> last12MonthsData = getMonthlyRevenue(sellerId, today.getYear());

        return RevenueAnalyticsResponse.DashboardMetrics.builder()
                .todayRevenue(todayRevenue)
                .monthRevenue(monthRevenue)
                .yearRevenue(yearRevenue)
                .totalRevenue(totalRevenue)
                .todayGrowth(todayGrowth)
                .monthGrowth(monthGrowth)
                .yearGrowth(yearGrowth)
                .todayTransactions(todayTransactions)
                .monthTransactions(monthTransactions)
                .yearTransactions(yearTransactions)
                .averageOrderValue(averageOrderValue)
                .last30Days(last30DaysData)
                .last12Months(last12MonthsData)
                .build();
    }

    /**
     * Get quick revenue summary statistics
     */
    @Transactional(readOnly = true)
    public RevenueAnalyticsResponse.RevenueSummary getQuickStats(Long sellerId) {
        LocalDate today = LocalDate.now();
        LocalDate yearStart = today.withDayOfYear(1);

        // Get total revenue and transactions for this year
        BigDecimal totalRevenue = salesRevenueRepository.findTotalRevenueBySeller(sellerId);
        BigDecimal yearRevenue = salesRevenueRepository.findTotalRevenueBySellerAndYear(sellerId, today.getYear());

        // Calculate additional metrics
        BigDecimal averageOrderValue = salesRevenueRepository.findAverageOrderValueBySeller(sellerId);

        // Get total transactions count - you might need to implement this query
        Integer totalTransactions =
                salesRevenueRepository.findTotalTransactionsBySellerAndYear(sellerId, today.getYear());

        // Calculate net revenue (assuming 5% fees/refunds)
        BigDecimal netRevenue = totalRevenue.multiply(BigDecimal.valueOf(0.95));
        BigDecimal totalRefunds = totalRevenue.multiply(BigDecimal.valueOf(0.05));

        // Calculate year-over-year growth
        LocalDate lastYear = today.minusYears(1);
        BigDecimal lastYearRevenue =
                salesRevenueRepository.findTotalRevenueBySellerAndYear(sellerId, lastYear.getYear());
        BigDecimal overallGrowthPercentage = calculateGrowthPercentage(yearRevenue, lastYearRevenue);

        return RevenueAnalyticsResponse.RevenueSummary.builder()
                .totalRevenue(totalRevenue)
                .netRevenue(netRevenue)
                .totalRefunds(totalRefunds)
                .totalTransactions(totalTransactions != null ? totalTransactions : 0)
                .averageOrderValue(averageOrderValue)
                .overallGrowthPercentage(overallGrowthPercentage)
                .periodStart(yearStart)
                .periodEnd(today)
                .build();
    }

    // Private helper methods

    private DailyMetrics calculateDailyMetrics(Long sellerId, LocalDate date) {
        try {
            // Query actual order data for the specific date
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            // Calculate actual metrics from SubOrder table
            List<Object[]> results = subOrderRepository.findDailyMetricsBySeller(sellerId, startOfDay, endOfDay);

            if (results.isEmpty()) {
                // No orders found for this date
                return DailyMetrics.builder()
                        .totalRevenue(BigDecimal.ZERO)
                        .netRevenue(BigDecimal.ZERO)
                        .transactions(0)
                        .averageOrderValue(BigDecimal.ZERO)
                        .refunds(0)
                        .refundAmount(BigDecimal.ZERO)
                        .build();
            }

            Object[] result = results.get(0);
            BigDecimal totalRevenue = (BigDecimal) result[0];
            Integer transactions = ((Number) result[1]).intValue();
            BigDecimal refundAmount = (BigDecimal) result[2];
            Integer refunds = ((Number) result[3]).intValue();

            // Calculate derived metrics
            BigDecimal netRevenue = totalRevenue.subtract(refundAmount != null ? refundAmount : BigDecimal.ZERO);
            BigDecimal averageOrderValue = transactions > 0
                    ? totalRevenue.divide(BigDecimal.valueOf(transactions), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return DailyMetrics.builder()
                    .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                    .netRevenue(netRevenue)
                    .transactions(transactions)
                    .averageOrderValue(averageOrderValue)
                    .refunds(refunds)
                    .refundAmount(refundAmount != null ? refundAmount : BigDecimal.ZERO)
                    .build();

        } catch (Exception e) {
            log.error("Error calculating daily metrics for seller {} on date {}: {}", sellerId, date, e.getMessage());
            // Return zero values on error, but log the issue
            return DailyMetrics.builder()
                    .totalRevenue(BigDecimal.ZERO)
                    .netRevenue(BigDecimal.ZERO)
                    .transactions(0)
                    .averageOrderValue(BigDecimal.ZERO)
                    .refunds(0)
                    .refundAmount(BigDecimal.ZERO)
                    .build();
        }
    }

    private BigDecimal calculateGrowthPercentage(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDailyGrowthPercentage(Long sellerId, LocalDate date, BigDecimal currentRevenue) {
        LocalDate previousDay = date.minusDays(1);
        BigDecimal previousRevenue = salesRevenueRepository.findTotalRevenueBySellerAndDate(sellerId, previousDay);
        return calculateGrowthPercentage(currentRevenue, previousRevenue);
    }

    private BigDecimal calculateMonthlyGrowthPercentage(
            Long sellerId, Integer year, Integer month, BigDecimal currentRevenue) {
        LocalDate lastMonth = LocalDate.of(year, month, 1).minusMonths(1);
        BigDecimal previousRevenue = salesRevenueRepository.findTotalRevenueBySellerAndMonth(
                sellerId, lastMonth.getYear(), lastMonth.getMonthValue());
        return calculateGrowthPercentage(currentRevenue, previousRevenue);
    }

    private BigDecimal calculateYearlyGrowthPercentage(Long sellerId, Integer year, BigDecimal currentRevenue) {
        BigDecimal previousRevenue = salesRevenueRepository.findTotalRevenueBySellerAndYear(sellerId, year - 1);
        return calculateGrowthPercentage(currentRevenue, previousRevenue);
    }

    @lombok.Data
    @lombok.Builder
    private static class DailyMetrics {
        BigDecimal totalRevenue;
        BigDecimal netRevenue;
        Integer transactions;
        BigDecimal averageOrderValue;
        Integer refunds;
        BigDecimal refundAmount;
    }
}
