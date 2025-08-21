package com.tawn.tawnht.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tawn.tawnht.entity.SalesRevenue;
import com.tawn.tawnht.entity.Seller;

@Repository
public interface SalesRevenueRepository extends JpaRepository<SalesRevenue, Long> {

    // Find by seller and date
    Optional<SalesRevenue> findBySellerAndSalesDate(Seller seller, LocalDate salesDate);

    // Daily revenue queries
    @Query("SELECT sr FROM SalesRevenue sr WHERE sr.seller.id = :sellerId "
            + "AND sr.salesDate BETWEEN :startDate AND :endDate ORDER BY sr.salesDate DESC")
    List<SalesRevenue> findDailyRevenueBySeller(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT sr FROM SalesRevenue sr WHERE sr.seller.id = :sellerId "
            + "AND sr.salesDate BETWEEN :startDate AND :endDate ORDER BY sr.salesDate DESC")
    Page<SalesRevenue> findDailyRevenueBySeller(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // Monthly aggregations - sử dụng Object[] thay vì constructor
    @Query("SELECT sr.year, sr.month, " + "CASE sr.month "
            + "WHEN 1 THEN 'January' WHEN 2 THEN 'February' WHEN 3 THEN 'March' "
            + "WHEN 4 THEN 'April' WHEN 5 THEN 'May' WHEN 6 THEN 'June' "
            + "WHEN 7 THEN 'July' WHEN 8 THEN 'August' WHEN 9 THEN 'September' "
            + "WHEN 10 THEN 'October' WHEN 11 THEN 'November' WHEN 12 THEN 'December' "
            + "END, "
            + "SUM(sr.totalRevenue), SUM(sr.netRevenue), SUM(sr.numberOfTransactions), "
            + "AVG(sr.averageOrderValue) "
            + "FROM SalesRevenue sr WHERE sr.seller.id = :sellerId "
            + "AND sr.year = :year GROUP BY sr.year, sr.month ORDER BY sr.month")
    List<Object[]> findMonthlyRevenueDataBySeller(@Param("sellerId") Long sellerId, @Param("year") Integer year);

    // Yearly aggregations - sử dụng Object[] thay vì constructor
    @Query("SELECT sr.year, SUM(sr.totalRevenue), SUM(sr.netRevenue), "
            + "SUM(sr.numberOfTransactions), AVG(sr.averageOrderValue) "
            + "FROM SalesRevenue sr WHERE sr.seller.id = :sellerId "
            + "GROUP BY sr.year ORDER BY sr.year DESC")
    List<Object[]> findYearlyRevenueDataBySeller(@Param("sellerId") Long sellerId);

    // Summary queries
    @Query("SELECT COALESCE(SUM(sr.totalRevenue), 0) FROM SalesRevenue sr "
            + "WHERE sr.seller.id = :sellerId AND sr.salesDate = :date")
    BigDecimal findTotalRevenueBySellerAndDate(@Param("sellerId") Long sellerId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(sr.totalRevenue), 0) FROM SalesRevenue sr "
            + "WHERE sr.seller.id = :sellerId AND sr.year = :year AND sr.month = :month")
    BigDecimal findTotalRevenueBySellerAndMonth(
            @Param("sellerId") Long sellerId, @Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT COALESCE(SUM(sr.totalRevenue), 0) FROM SalesRevenue sr "
            + "WHERE sr.seller.id = :sellerId AND sr.year = :year")
    BigDecimal findTotalRevenueBySellerAndYear(@Param("sellerId") Long sellerId, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(sr.totalRevenue), 0) FROM SalesRevenue sr WHERE sr.seller.id = :sellerId")
    BigDecimal findTotalRevenueBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COALESCE(SUM(sr.numberOfTransactions), 0) FROM SalesRevenue sr "
            + "WHERE sr.seller.id = :sellerId AND sr.salesDate = :date")
    Integer findTotalTransactionsBySellerAndDate(@Param("sellerId") Long sellerId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(sr.numberOfTransactions), 0) FROM SalesRevenue sr "
            + "WHERE sr.seller.id = :sellerId AND sr.year = :year AND sr.month = :month")
    Integer findTotalTransactionsBySellerAndMonth(
            @Param("sellerId") Long sellerId, @Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT COALESCE(SUM(sr.numberOfTransactions), 0) FROM SalesRevenue sr "
            + "WHERE sr.seller.id = :sellerId AND sr.year = :year")
    Integer findTotalTransactionsBySellerAndYear(@Param("sellerId") Long sellerId, @Param("year") Integer year);

    @Query("SELECT COALESCE(AVG(sr.averageOrderValue), 0) FROM SalesRevenue sr WHERE sr.seller.id = :sellerId")
    BigDecimal findAverageOrderValueBySeller(@Param("sellerId") Long sellerId);

    // Growth calculations
    @Query("SELECT COALESCE(SUM(sr.totalRevenue), 0) FROM SalesRevenue sr "
            + "WHERE sr.seller.id = :sellerId AND sr.salesDate BETWEEN :startDate AND :endDate")
    BigDecimal findRevenueBySellerAndDateRange(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Top performing periods
    @Query("SELECT sr FROM SalesRevenue sr WHERE sr.seller.id = :sellerId " + "ORDER BY sr.totalRevenue DESC")
    Page<SalesRevenue> findTopPerformingDays(@Param("sellerId") Long sellerId, Pageable pageable);

    // Recent revenue data for dashboard
    @Query("SELECT sr FROM SalesRevenue sr WHERE sr.seller.id = :sellerId "
            + "AND sr.salesDate >= :since ORDER BY sr.salesDate DESC")
    List<SalesRevenue> findRecentRevenueBySeller(@Param("sellerId") Long sellerId, @Param("since") LocalDate since);
}
