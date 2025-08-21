package com.tawn.tawnht.controller;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.response.PageResponse;
import com.tawn.tawnht.dto.response.RevenueAnalyticsResponse;
import com.tawn.tawnht.service.SalesRevenueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Sales Revenue", description = "Sales revenue tracking and analytics API")
public class SalesRevenueController {

    SalesRevenueService salesRevenueService;

    @GetMapping("/dashboard/{sellerId}")
    @Operation(
            summary = "Get comprehensive dashboard metrics",
            description = "Retrieve all key metrics including today, month, year totals with growth rates")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse.DashboardMetrics>> getDashboardMetrics(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId) {

        log.info("Getting dashboard metrics for seller: {}", sellerId);

        RevenueAnalyticsResponse.DashboardMetrics metrics = salesRevenueService.getDashboardMetrics(sellerId);

        return ResponseEntity.ok(ApiResponse.<RevenueAnalyticsResponse.DashboardMetrics>builder()
                .code(1000)
                .message("Dashboard metrics retrieved successfully")
                .result(metrics)
                .build());
    }

    @GetMapping("/daily/{sellerId}")
    @Operation(
            summary = "Get daily revenue data",
            description = "Retrieve daily revenue data for a specific date range")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<RevenueAnalyticsResponse.DailyRevenue>>> getDailyRevenue(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @Parameter(description = "Start date (YYYY-MM-DD)")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {

        log.info("Getting daily revenue for seller: {} from {} to {}", sellerId, startDate, endDate);

        List<RevenueAnalyticsResponse.DailyRevenue> dailyRevenue =
                salesRevenueService.getDailyRevenue(sellerId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.<List<RevenueAnalyticsResponse.DailyRevenue>>builder()
                .code(1000)
                .message("Daily revenue data retrieved successfully")
                .result(dailyRevenue)
                .build());
    }

    @GetMapping("/daily/{sellerId}/paginated")
    @Operation(
            summary = "Get paginated daily revenue data",
            description = "Retrieve daily revenue data with pagination support")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<PageResponse<RevenueAnalyticsResponse.DailyRevenue>>> getDailyRevenuePaginated(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @Parameter(description = "Start date (YYYY-MM-DD)")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "salesDate") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info(
                "Getting paginated daily revenue for seller: {} from {} to {}, page: {}, size: {}",
                sellerId,
                startDate,
                endDate,
                page,
                size);

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<RevenueAnalyticsResponse.DailyRevenue> dailyRevenuePage =
                salesRevenueService.getDailyRevenue(sellerId, startDate, endDate, pageable);

        PageResponse<RevenueAnalyticsResponse.DailyRevenue> response =
                PageResponse.<RevenueAnalyticsResponse.DailyRevenue>builder()
                        .currentPage(dailyRevenuePage.getNumber())
                        .totalPages(dailyRevenuePage.getTotalPages())
                        .pageSize(dailyRevenuePage.getSize())
                        .totalElements(dailyRevenuePage.getTotalElements())
                        .data(dailyRevenuePage.getContent())
                        .build();

        return ResponseEntity.ok(ApiResponse.<PageResponse<RevenueAnalyticsResponse.DailyRevenue>>builder()
                .code(1000)
                .message("Paginated daily revenue data retrieved successfully")
                .result(response)
                .build());
    }

    @GetMapping("/monthly/{sellerId}")
    @Operation(summary = "Get monthly revenue data", description = "Retrieve monthly revenue data for a specific year")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<RevenueAnalyticsResponse.MonthlyRevenue>>> getMonthlyRevenue(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @Parameter(description = "Year (YYYY)") @RequestParam(required = false) Integer year) {

        // Default to current year if not provided
        if (year == null) {
            year = Year.now().getValue();
        }

        log.info("Getting monthly revenue for seller: {} for year: {}", sellerId, year);

        List<RevenueAnalyticsResponse.MonthlyRevenue> monthlyRevenue =
                salesRevenueService.getMonthlyRevenue(sellerId, year);

        return ResponseEntity.ok(ApiResponse.<List<RevenueAnalyticsResponse.MonthlyRevenue>>builder()
                .code(1000)
                .message("Monthly revenue data retrieved successfully")
                .result(monthlyRevenue)
                .build());
    }

    @GetMapping("/yearly/{sellerId}")
    @Operation(summary = "Get yearly revenue data", description = "Retrieve yearly revenue data for all years")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<RevenueAnalyticsResponse.YearlyRevenue>>> getYearlyRevenue(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId) {

        log.info("Getting yearly revenue for seller: {}", sellerId);

        List<RevenueAnalyticsResponse.YearlyRevenue> yearlyRevenue = salesRevenueService.getYearlyRevenue(sellerId);

        return ResponseEntity.ok(ApiResponse.<List<RevenueAnalyticsResponse.YearlyRevenue>>builder()
                .code(1000)
                .message("Yearly revenue data retrieved successfully")
                .result(yearlyRevenue)
                .build());
    }

    @GetMapping("/quick-stats/{sellerId}")
    @Operation(
            summary = "Get quick revenue statistics",
            description = "Get today, this week, this month, and this year revenue totals")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse.RevenueSummary>> getQuickStats(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId) {

        log.info("Getting quick stats for seller: {}", sellerId);

        // Get real quick summary statistics from service
        RevenueAnalyticsResponse.RevenueSummary summary = salesRevenueService.getQuickStats(sellerId);

        return ResponseEntity.ok(ApiResponse.<RevenueAnalyticsResponse.RevenueSummary>builder()
                .code(1000)
                .message("Quick statistics retrieved successfully")
                .result(summary)
                .build());
    }

    @PostMapping("/update/{sellerId}")
    @Operation(
            summary = "Update daily sales revenue",
            description = "Manually update or recalculate daily sales revenue for a specific date")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<String>> updateDailySalesRevenue(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @Parameter(description = "Date to update (YYYY-MM-DD)")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {

        log.info("Updating daily sales revenue for seller: {} on date: {}", sellerId, date);

        salesRevenueService.updateDailySalesRevenue(sellerId, date);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("Daily sales revenue updated successfully")
                .result("Revenue data updated for " + date)
                .build());
    }
}
