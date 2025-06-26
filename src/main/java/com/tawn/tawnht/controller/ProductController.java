package com.tawn.tawnht.controller;

import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.request.PermissionRequest;
import com.tawn.tawnht.dto.request.ProductCreationRequest;
import com.tawn.tawnht.dto.request.RoleRequest;
import com.tawn.tawnht.dto.response.*;
import com.tawn.tawnht.dto.response.ProductResponse;
import com.tawn.tawnht.dto.response.ProductResponse;
import com.tawn.tawnht.entity.Product;
import com.tawn.tawnht.service.ProductService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.query.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductController {
    ProductService productService;
    @PostMapping
    ApiResponse<ProductResponse> create(@RequestBody ProductCreationRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }
    @GetMapping
    ApiResponse<PageResponse<ProductResponse>> getAll(
            @Filter Specification<Product> spec,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size)
     {
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .result(productService.getAllProduct(spec, page, size))
                .build();
    }
    @GetMapping("/{id}")
    ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(id))
                .build();
    }
}
