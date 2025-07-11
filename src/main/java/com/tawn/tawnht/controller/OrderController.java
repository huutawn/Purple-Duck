package com.tawn.tawnht.controller;

import com.tawn.tawnht.dto.request.AddressCreationRequest;
import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.request.OrderCreationRequest;
import com.tawn.tawnht.dto.request.ProductCreationRequest;
import com.tawn.tawnht.dto.response.PageResponse;
import com.tawn.tawnht.dto.response.ProductResponse;
import com.tawn.tawnht.dto.response.OrderResponse;
import com.tawn.tawnht.dto.response.SubOrderResponse;
import com.tawn.tawnht.entity.Order;
import com.tawn.tawnht.entity.Product;
import com.tawn.tawnht.service.OrderService;
import com.tawn.tawnht.service.ProductService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class OrderController {
    OrderService orderService;
    @PostMapping
    ApiResponse<OrderResponse> upload(@RequestBody OrderCreationRequest request)  {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(request))
                .build();
    }
    @GetMapping
    ApiResponse<PageResponse<SubOrderResponse>> getAll(
            @Filter Specification<Order> spec,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size)
    {
        return ApiResponse.<PageResponse<SubOrderResponse>>builder()
                .result(orderService.getAllOrderBySeller(spec, page, size))
                .build();
    }

}
