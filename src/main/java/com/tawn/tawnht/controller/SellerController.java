package com.tawn.tawnht.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.request.SellerCreationRequest;
import com.tawn.tawnht.dto.response.CustomerResponse;
import com.tawn.tawnht.dto.response.NotificationResponse;
import com.tawn.tawnht.dto.response.PageResponse;
import com.tawn.tawnht.dto.response.SellerProfileResponse;
import com.tawn.tawnht.dto.response.SellerResponse;
import com.tawn.tawnht.service.CustomerService;
import com.tawn.tawnht.service.NotificationService;
import com.tawn.tawnht.service.SellerService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SellerController {
    SellerService sellerService;
    CustomerService customerService;
    NotificationService notificationService;

    @PostMapping
    ApiResponse<SellerResponse> create(@RequestBody SellerCreationRequest request) {
        return ApiResponse.<SellerResponse>builder()
                .result(sellerService.createSeller(request))
                .build();
    }

    @GetMapping("/profile")
    ApiResponse<SellerProfileResponse> getSellerProfile() {
        return ApiResponse.<SellerProfileResponse>builder()
                .result(sellerService.getSellerProfile())
                .build();
    }

    @GetMapping("/notifications")
    ApiResponse<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<NotificationResponse> notifications = notificationService.getSellerNotifications(pageable);

        return ApiResponse.<PageResponse<NotificationResponse>>builder()
                .result(notifications)
                .build();
    }

    @GetMapping("/notifications/count")
    ApiResponse<Integer> getUnreadNotificationCount() {
        return ApiResponse.<Integer>builder()
                .result(notificationService.getUnreadNotificationCount())
                .build();
    }

    @GetMapping("/customers")
    ApiResponse<PageResponse<CustomerResponse>> getCustomers(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<CustomerResponse> customers = customerService.getCustomersBySeller(pageable, search);

        return ApiResponse.<PageResponse<CustomerResponse>>builder()
                .result(customers)
                .build();
    }
}
