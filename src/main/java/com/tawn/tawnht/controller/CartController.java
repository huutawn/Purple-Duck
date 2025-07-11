package com.tawn.tawnht.controller;

import com.tawn.tawnht.dto.request.AddToCartReq;
import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.request.OrderCreationRequest;
import com.tawn.tawnht.dto.response.*;
import com.tawn.tawnht.entity.Order;
import com.tawn.tawnht.service.CartService;
import com.tawn.tawnht.service.OrderService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class CartController {
    CartService cartService;
    @PostMapping
    ApiResponse<CartResponse> create(@RequestBody List<AddToCartReq> items)  {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addToCart(items))
                .build();
    }
    @GetMapping
    ApiResponse<List<CartItemResponse>> getAll()
    {
        return ApiResponse.<List<CartItemResponse>>builder()
                .result(cartService.getAllItem())
                .build();
    }

}
