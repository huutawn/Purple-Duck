package com.tawn.tawnht.controller;

import org.springframework.web.bind.annotation.*;

import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.request.SePayWebhookRequest;
import com.tawn.tawnht.dto.response.TransactionResponse;
import com.tawn.tawnht.service.SePayWebHookService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/hooks/sepay-payment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@RestController
public class SePayWebController {

    SePayWebHookService sePayWebhookService;

    @PostMapping
    ApiResponse<TransactionResponse> create(@RequestBody SePayWebhookRequest request) {
        return ApiResponse.<TransactionResponse>builder()
                .result(sePayWebhookService.getTransaction(request))
                .build();
    }
}
