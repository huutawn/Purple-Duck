package com.tawn.tawnht.mapper;

import com.tawn.tawnht.dto.response.*;
import com.tawn.tawnht.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductMapper {
    // Ánh xạ Product sang ProductResponse
    public ProductResponse toProductResponse(Product product) {
        if (product == null) return null;

        log.info("Mapping Product with ID: {} to ProductResponse", product.getId());

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

    private List<ProductImageResponse> toProductImageResponses(Set<ProductImage> images) {
        if (images == null) return new ArrayList<>();
        return images.stream()
                .map(this::toProductImageResponse)
                .collect(Collectors.toList());
    }

    private ProductImageResponse toProductImageResponse(ProductImage image) {
        if (image == null) return null;

        return ProductImageResponse.builder()
                .id(image.getId())
                .productId(image.getProduct() != null ? image.getProduct().getId() : null)
                .imageUrl(image.getImageUrl())
                .displayOrder(image.getDisplayOrder())
                .createdAt(image.getCreatedAt())
                .build();
    }

    private List<ProductVariantResponse> toProductVariantResponses(Set<ProductVariant> variants) {
        if (variants == null) return new ArrayList<>();
        return variants.stream()
                .map(this::toProductVariantResponse)
                .collect(Collectors.toList());
    }

    private ProductVariantResponse toProductVariantResponse(ProductVariant variant) {
        if (variant == null) return null;

        log.info("Mapping ProductVariant with ID: {} to ProductVariantResponse", variant.getId());

        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct() != null ? variant.getProduct().getId() : null)
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stock(variant.getStock())
                .image(variant.getImage())
                .attributes(toAttributeResponses(variant.getProductVariantAttributes()))
                .build();
    }

    private List<AttributeResponse> toAttributeResponses(Set<ProductVariantAttribute> variantAttributes) {
        if (variantAttributes == null) return new ArrayList<>();

        log.info("Mapping {} ProductVariantAttributes to AttributeResponses", variantAttributes.size());

        // Nhóm các thuộc tính theo ProductAttribute
        Map<ProductAttribute, List<ProductAttributeValue>> groupedAttributes = variantAttributes.stream()
                .collect(Collectors.groupingBy(
                        attr -> attr.getProductAttributeValue().getProductAttribute(),
                        Collectors.mapping(
                                ProductVariantAttribute::getProductAttributeValue,
                                Collectors.toList()
                        )
                ));

        return groupedAttributes.entrySet().stream()
                .map(entry -> {
                    ProductAttribute attribute = entry.getKey();
                    List<ProductAttributeValue> values = entry.getValue();

                    AttributeResponse response = new AttributeResponse();
                    response.setAttributeId(attribute.getId());
                    response.setAttributeName(attribute.getDisplayName() != null ? attribute.getDisplayName() : attribute.getName());

                    List<AttributeValueResponse> valueResponses = values.stream()
                            .map(val -> AttributeValueResponse.builder()
                                    .attributeValueId(val.getId())
                                    .value(val.getValue())
                                    .displayValue(val.getDisplayValue() != null ? val.getDisplayValue() : val.getValue())
                                    .build())
                            .collect(Collectors.toList());

                    response.setAttributeValue(valueResponses);
                    return response;
                })
                .collect(Collectors.toList());
    }
}
