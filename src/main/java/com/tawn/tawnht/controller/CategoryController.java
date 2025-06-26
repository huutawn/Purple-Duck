package com.tawn.tawnht.controller;

import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.entity.Category;
import com.tawn.tawnht.service.CategoryService;
import com.tawn.tawnht.service.CloudinaryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class CategoryController {
    CategoryService categoryService;
    @PostMapping
    ApiResponse<String> upload(@RequestBody List<String> names)  {
        return ApiResponse.<String>builder()
                .result(categoryService.createCategory(names))
                .build();
    }
    @GetMapping
    ApiResponse<List<Category>> get()  {
        return ApiResponse.<List<Category>>builder()
                .result(categoryService.getAll())
                .build();
    }

}
