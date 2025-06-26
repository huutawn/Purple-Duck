package com.tawn.tawnht.controller;

import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.request.ProductCreationRequest;
import com.tawn.tawnht.dto.response.PageResponse;
import com.tawn.tawnht.dto.response.ProductResponse;
import com.tawn.tawnht.entity.Product;
import com.tawn.tawnht.service.CloudinaryService;
import com.tawn.tawnht.service.ProductService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/cloudinary")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class CloudinaryController {
    CloudinaryService cloudinaryService;
    @PostMapping
    ApiResponse<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<String>builder()
                .result(cloudinaryService.uploadFile(file,"",""))
                .build();
    }

}
