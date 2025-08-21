package com.tawn.tawnht.mapper;

import org.mapstruct.Mapper;

import com.tawn.tawnht.dto.response.ProductVariantResponse;
import com.tawn.tawnht.entity.ProductVariant;

@Mapper(componentModel = "spring")
public interface VariantMapper {
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);
}
