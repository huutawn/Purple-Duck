package com.tawn.tawnht.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.tawn.tawnht.dto.request.AddToCartReq;
import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.response.*;
import com.tawn.tawnht.service.CartService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @PostMapping
    ApiResponse<CartResponse> create(@RequestBody AddToCartReq items) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addToCart(items))
                .build();
    }

    @GetMapping
    ApiResponse<List<CartItemResponse>> getAll() {
        return ApiResponse.<List<CartItemResponse>>builder()
                .result(cartService.getAllItem())
                .build();
    }
}
