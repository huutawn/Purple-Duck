package com.tawn.tawnht.service;

import com.tawn.tawnht.dto.request.AddressCreationRequest;
import com.tawn.tawnht.dto.request.SellerCreationRequest;
import com.tawn.tawnht.dto.response.SellerResponse;
import com.tawn.tawnht.dto.response.UserAddressResponse;
import com.tawn.tawnht.entity.Seller;
import com.tawn.tawnht.entity.User;
import com.tawn.tawnht.entity.UserAddress;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.mapper.OrderMapper;
import com.tawn.tawnht.mapper.SellerMapper;
import com.tawn.tawnht.repository.jpa.SellerRepository;
import com.tawn.tawnht.repository.jpa.UserAddressRepository;
import com.tawn.tawnht.repository.jpa.UserRepository;
import com.tawn.tawnht.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class AddressService {
    UserRepository userRepository;
    UserAddressRepository userAddressRepository;
    OrderMapper orderMapper;
    public UserAddressResponse createAddress(AddressCreationRequest request){
        String email=SecurityUtils.getCurrentUserLogin()
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        UserAddress userAddress=UserAddress.builder()
                .address(request.getAddress())
                .createdAt(LocalDateTime.now())
                .addressType(request.getAddressType())
                .city(request.getCity())
                .user(user)
                .commune(request.getCommune())
                .district(request.getDistrict())
                .isDefault(request.getIsDefault())
                .build();
        return orderMapper.toUserAddressResponse(userAddressRepository.save(userAddress));
    }
    public UserAddressResponse getCurrentUserAddress(Long id){
        UserAddress userAddress=userAddressRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        return orderMapper.toUserAddressResponse(userAddress);
    }
    public List<UserAddressResponse> getAllCurrentUserAddress(){
        String email=SecurityUtils.getCurrentUserLogin()
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        Set<UserAddress> userAddresses=user.getUserAddresses();
        return  userAddresses.stream().map(orderMapper::toUserAddressResponse).toList();
    }
}
