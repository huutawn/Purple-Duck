package com.tawn.tawnht.mapper;

import org.mapstruct.Mapper;

import com.tawn.tawnht.dto.response.SellerResponse;
import com.tawn.tawnht.entity.Seller;

@Mapper(componentModel = "spring")
public interface SellerMapper {
    SellerResponse toSellerResponse(Seller seller);
}
