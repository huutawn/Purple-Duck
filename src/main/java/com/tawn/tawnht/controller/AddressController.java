package com.tawn.tawnht.controller;

import com.tawn.tawnht.dto.request.AddressCreationRequest;
import com.tawn.tawnht.dto.request.ApiResponse;
import com.tawn.tawnht.dto.response.UserAddressResponse;
import com.tawn.tawnht.entity.Category;
import com.tawn.tawnht.service.AddressService;
import com.tawn.tawnht.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AddressController {
    AddressService addressService;
    @PostMapping
    ApiResponse<UserAddressResponse> upload(@RequestBody AddressCreationRequest request)  {
        return ApiResponse.<UserAddressResponse>builder()
                .result(addressService.createAddress(request))
                .build();
    }
    @GetMapping
    ApiResponse<List<UserAddressResponse>> get()  {
        return ApiResponse.<List<UserAddressResponse>>builder()
                .result(addressService.getAllCurrentUserAddress())
                .build();
    }
    @GetMapping("/{id}")
    ApiResponse<UserAddressResponse> getCurrentAddress(@PathVariable Long id)  {
        return ApiResponse.<UserAddressResponse>builder()
                .result(addressService.getCurrentUserAddress(id))
                .build();
    }

}
