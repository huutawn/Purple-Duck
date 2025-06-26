package com.tawn.tawnht.mapper;

import com.tawn.tawnht.dto.response.*;
import com.tawn.tawnht.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    // Ánh xạ Product sang ProductResponse
    public ProductResponse toProductResponse(Product product) {
        if (product == null) return null;

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        response.setSellerId(product.getSeller() != null ? product.getSeller().getId() : null);
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSlug(product.getSlug());
        response.setCoverImage(product.getCoverImage());
        response.setImages(toProductImageResponses(product.getImages()));
        response.setMetaTitle(product.getMetaTitle());
        response.setPurchase(product.getPurchase());
        response.setMetaDescription(product.getMetaDescription());
        response.setWarrantyInfo(product.getWarrantyInfo());
        response.setCreatedAt(product.getCreatedAt());
        response.setProductVariants(toProductVariantResponses(product.getProductVariants()));
        response.setActive(product.isActive());
        return response;
    }


    // Ánh xạ Set<ProductVariant> sang List<ProductVariantResponse>
    private List<ProductVariantResponse> toProductVariantResponses(Set<ProductVariant> variants) {
        if (variants == null) return null;
        return variants.stream()
                .map(this::toProductVariantResponse)
                .collect(Collectors.toList());
    }

    // Ánh xạ ProductVariant sang ProductVariantResponse
    private ProductVariantResponse toProductVariantResponse(ProductVariant variant) {
        if (variant == null) return null;

        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(variant.getId());
        response.setProductId(variant.getProduct() != null ? variant.getProduct().getId() : null);
        response.setProductName(variant.getProduct() != null ? variant.getProduct().getName() : null);
        response.setSku(variant.getSku());
        response.setPrice(variant.getPrice());
        response.setStock(variant.getStock());
        response.setImage(variant.getImage());
        response.setAttributes(toAttributeResponses(variant.getProductVariantAttributes(), variant.getProduct()));
        return response;
    }

    // Ánh xạ Set<ProductVariantAttribute> sang List<AttributeResponse>
    private List<AttributeResponse> toAttributeResponses(Set<ProductVariantAttribute> variantAttributes, Product product) {
        if (variantAttributes == null || product == null) return null;

        // Lấy tất cả ProductAttribute và ProductAttributeValue liên quan
        Set<ProductAttribute> allAttributes = product.getProductVariants().stream()
                .flatMap(v -> v.getProductVariantAttributes().stream())
                .map(attr -> attr.getProductAttributeValue().getProductAttribute())
                .collect(Collectors.toSet());

        // Nhóm các giá trị thuộc tính đã chọn theo ProductAttribute
        Map<ProductAttribute, ProductAttributeValue> selectedMap = variantAttributes.stream()
                .collect(Collectors.toMap(
                        attr -> attr.getProductAttributeValue().getProductAttribute(),
                        ProductVariantAttribute::getProductAttributeValue
                ));

        return allAttributes.stream()
                .map(attribute -> {
                    AttributeResponse response = new AttributeResponse();
                    response.setAttributeId(attribute.getId());
                    response.setAttributeName(attribute.getDisplayName());



                    // Tất cả giá trị có thể
                    response.setAttributeValue(attribute.getProductAttributeValue().stream()
                            .map(val -> {
                                AttributeValueResponse valRes = new AttributeValueResponse();
                                valRes.setAttributeValueId(val.getId());
                                valRes.setValue(val.getValue());
                                valRes.setDisplayValue(val.getDisplayValue());
                                return valRes;
                            })
                            .collect(Collectors.toList()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    // Ánh xạ Set<ProductImage> sang List<ProductImageResponse>
    private List<ProductImageResponse> toProductImageResponses(Set<ProductImage> images) {
        if (images == null) return null;
        return images.stream()
                .map(this::toProductImageResponse)
                .collect(Collectors.toList());
    }

    // Ánh xạ ProductImage sang ProductImageResponse
    private ProductImageResponse toProductImageResponse(ProductImage image) {
        if (image == null) return null;

        ProductImageResponse response = new ProductImageResponse();
        response.setId(image.getId());
        response.setProductId(image.getProduct() != null ? image.getProduct().getId() : null);
        response.setImageUrl(image.getImageUrl());
        response.setDisplayOrder(image.getDisplayOrder());
        response.setCreatedAt(image.getCreatedAt());
        return response;
    }
}
