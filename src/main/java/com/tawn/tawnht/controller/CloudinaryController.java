package com.tawn.tawnht.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.WriterException;
import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.service.CloudinaryService;
import com.tawn.tawnht.service.QRService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/cloudinary")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryController {
    CloudinaryService cloudinaryService;
    QRService qrService;

    @PostMapping
    ApiResponse<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<String>builder()
                .result(cloudinaryService.uploadFile(file, "", ""))
                .build();
    }

    @PostMapping("/qr")
    ApiResponse<String> uploadQr(String amount, String content) throws IOException, WriterException {
        return ApiResponse.<String>builder()
                .result(cloudinaryService.uploadFile(qrService.generateBankQrFile(amount, content, ""), "", ""))
                .build();
    }
}
