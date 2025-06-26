package com.tawn.tawnht.mapper;

import com.tawn.tawnht.dto.response.ProductVariantResponse;
import com.tawn.tawnht.entity.ProductVariant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface VariantMapper {
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);
}
