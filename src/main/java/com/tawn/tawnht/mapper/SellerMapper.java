package com.tawn.tawnht.mapper;

import com.tawn.tawnht.dto.response.SellerResponse;
import com.tawn.tawnht.entity.Seller;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SellerMapper {
    SellerResponse toSellerResponse(Seller seller);
}
