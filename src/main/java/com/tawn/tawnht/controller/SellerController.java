package com.tawn.tawnht.controller;

import org.springframework.web.bind.annotation.*;

import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.request.SellerCreationRequest;
import com.tawn.tawnht.dto.response.SellerResponse;
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

    @PostMapping
    ApiResponse<SellerResponse> create(@RequestBody SellerCreationRequest request) {
        return ApiResponse.<SellerResponse>builder()
                .result(sellerService.createSeller(request))
                .build();
    }
}
