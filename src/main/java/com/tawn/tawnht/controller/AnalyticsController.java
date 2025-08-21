package com.tawn.tawnht.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.service.OrderAnalyticsService;
import com.tawn.tawnht.service.SalesRevenueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Analytics", description = "Comprehensive analytics for sellers")
public class AnalyticsController {

    SalesRevenueService salesRevenueService;
    OrderAnalyticsService orderAnalyticsService;

    @GetMapping("/overview/{sellerId}")
    @Operation(
            summary = "Get comprehensive analytics overview",
            description = "Get all analytics data including revenue, orders, products, and customers")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalyticsOverview(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @Parameter(description = "Start date (YYYY-MM-DD)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {

        // Default to last 30 days if no dates provided
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }

        log.info("Getting analytics overview for seller: {} from {} to {}", sellerId, startDate, endDate);

        Map<String, Object> overview = new HashMap<>();

        // Revenue analytics
        overview.put("revenueMetrics", salesRevenueService.getDashboardMetrics(sellerId));

        // Order analytics
        overview.put("orderStatistics", orderAnalyticsService.getOrderStatistics(sellerId, startDate, endDate));

        // Product performance
        overview.put("productPerformance", orderAnalyticsService.getProductPerformance(sellerId, startDate, endDate));

        // Customer analytics
        overview.put("customerAnalytics", orderAnalyticsService.getCustomerAnalytics(sellerId, startDate, endDate));

        // Period information
        overview.put("periodStart", startDate);
        overview.put("periodEnd", endDate);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(1000)
                .message("Analytics overview retrieved successfully")
                .result(overview)
                .build());
    }

    @GetMapping("/orders/{sellerId}")
    @Operation(summary = "Get order analytics", description = "Get detailed order statistics and performance metrics")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderAnalytics(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @Parameter(description = "Start date (YYYY-MM-DD)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {

        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(30);

        log.info("Getting order analytics for seller: {} from {} to {}", sellerId, startDate, endDate);

        Map<String, Object> analytics = orderAnalyticsService.getOrderStatistics(sellerId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(1000)
                .message("Order analytics retrieved successfully")
                .result(analytics)
                .build());
    }

    @GetMapping("/products/{sellerId}")
    @Operation(summary = "Get product performance analytics", description = "Get product sales performance and metrics")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductAnalytics(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @Parameter(description = "Start date (YYYY-MM-DD)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {

        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(30);

        log.info("Getting product analytics for seller: {} from {} to {}", sellerId, startDate, endDate);

        Map<String, Object> analytics = orderAnalyticsService.getProductPerformance(sellerId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(1000)
                .message("Product analytics retrieved successfully")
                .result(analytics)
                .build());
    }

    @GetMapping("/customers/{sellerId}")
    @Operation(
            summary = "Get customer analytics",
            description = "Get customer acquisition, retention and lifetime value metrics")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomerAnalytics(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @Parameter(description = "Start date (YYYY-MM-DD)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {

        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(30);

        log.info("Getting customer analytics for seller: {} from {} to {}", sellerId, startDate, endDate);

        Map<String, Object> analytics = orderAnalyticsService.getCustomerAnalytics(sellerId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(1000)
                .message("Customer analytics retrieved successfully")
                .result(analytics)
                .build());
    }

    @PostMapping("/refresh/{sellerId}")
    @Operation(summary = "Refresh analytics data", description = "Manually trigger analytics data refresh for a seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<String>> refreshAnalytics(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @Parameter(description = "Date to refresh (YYYY-MM-DD), defaults to today")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("Refreshing analytics for seller: {} on date: {}", sellerId, date);

        // Update sales revenue data
        salesRevenueService.updateDailySalesRevenue(sellerId, date);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("Analytics data refreshed successfully")
                .result("Data refreshed for " + date)
                .build());
    }
}
