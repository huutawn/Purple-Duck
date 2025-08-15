package com.tawn.tawnht.mapper;


import com.tawn.tawnht.document.ProductAttributeDocument;
import com.tawn.tawnht.document.ProductDocument;
import com.tawn.tawnht.document.ProductVariantDocument;
import com.tawn.tawnht.dto.response.*;
import com.tawn.tawnht.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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


        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        response.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
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
    public ProductResponse toProductResponse(ProductDocument document) {
        if (document == null) return null;

        ProductResponse response = new ProductResponse();
        response.setId(Long.valueOf(document.getId())); // Chuyển String ID từ Document sang Long ID của Response
        response.setCategoryId(document.getCategoryId());
        response.setCategoryName(document.getCategoryName());
        response.setSellerId(document.getSellerId());
        response.setName(document.getName());
        response.setDescription(document.getDescription());
        response.setSlug(document.getSlug());
        response.setCoverImage(document.getCoverImage());
        response.setImages(mapImageUrlsToProductImageResponses(document.getImageUrls())); // Ánh xạ từ imageUrls
        response.setMetaTitle(document.getMetaTitle());
        response.setPurchase(document.getPurchase());
        response.setMetaDescription(document.getMetaDescription());
        response.setWarrantyInfo(document.getWarrantyInfo());
        response.setProductVariants(mapProductVariantDocumentsToResponses(document.getVariants())); // Ánh xạ từ variants
        response.setActive(document.isActive());
        return response;
    }

    private List<ProductImageResponse> mapImageUrlsToProductImageResponses(List<String> imageUrls) {
        if (imageUrls == null) return new ArrayList<>();
        return imageUrls.stream()
                .map(url -> ProductImageResponse.builder().imageUrl(url).build())
                .collect(Collectors.toList());
    }

    private List<ProductVariantResponse> mapProductVariantDocumentsToResponses(List<ProductVariantDocument> variantDocuments) {
        if (variantDocuments == null) return new ArrayList<>();
        return variantDocuments.stream()
                .map(this::toProductVariantResponseFromDocument)
                .collect(Collectors.toList());
    }

    private ProductVariantResponse toProductVariantResponseFromDocument(ProductVariantDocument document) {
        if (document == null) return null;
        return ProductVariantResponse.builder()
                .sku(document.getSku())
                .price(document.getPrice())
                .stock(document.getStock())
                .image(document.getImageUrl())
                .attributes(mapProductAttributeDocumentsToAttributeResponses(document.getAttributes()))
                .build();
    }

    private List<AttributeResponse> mapProductAttributeDocumentsToAttributeResponses(List<ProductAttributeDocument> attributeDocuments) {
        if (attributeDocuments == null) return new ArrayList<>();
        // Lưu ý: Logic nhóm thuộc tính của bạn trong JPA mapper phức tạp hơn.
        // Ở đây, chúng ta giả định ProductAttributeDocument đã là cặp name-value đơn giản.
        return attributeDocuments.stream()
                .map(doc -> AttributeResponse.builder()
                        .attributeName(doc.getAttributeName())
                        .attributeValue(List.of(AttributeValueResponse.builder().value(doc.getAttributeValue()).build()))
                        .build())
                .collect(Collectors.toList());
    }


    // --- Ánh xạ từ JPA Entity sang Elasticsearch Document (để index) ---
    public ProductDocument toDocument(Product product) {
        if (product == null) return null;

        return ProductDocument.builder()
                .id(product.getId()+"") // Chuyển Long ID sang String cho ES Document
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .slug(product.getSlug())
                .coverImage(product.getCoverImage())
                .purchase(product.getPurchase())
                .price(product.getProductVariants() != null && !product.getProductVariants().isEmpty() ?
                        product.getProductVariants().stream().map(ProductVariant::getPrice).min(BigDecimal::compareTo).orElse(null) : null) // Lấy giá thấp nhất từ variants
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .warrantyInfo(product.getWarrantyInfo())
                .isActive(product.isActive())
                .imageUrls(mapProductImagesToUrls(product.getImages()))
                .variants(mapProductVariantsToDocuments(product.getProductVariants()))
                .build();
    }

    private List<String> mapProductImagesToUrls(Set<ProductImage> images) {
        if (images == null) return new ArrayList<>();
        return images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }

    private List<ProductVariantDocument> mapProductVariantsToDocuments(Set<ProductVariant> variants) {
        if (variants == null) return new ArrayList<>();
        return variants.stream()
                .map(this::toProductVariantDocument)
                .collect(Collectors.toList());
    }

    private ProductVariantDocument toProductVariantDocument(ProductVariant variant) {
        if (variant == null) return null;
        return ProductVariantDocument.builder()
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stock(variant.getStock())
                .imageUrl(variant.getImage()) // Assuming getImage() returns URL
                .attributes(mapProductVariantAttributesToProductAttributeDocuments(variant.getProductVariantAttributes()))
                .build();
    }

    private List<ProductAttributeDocument> mapProductVariantAttributesToProductAttributeDocuments(Set<ProductVariantAttribute> variantAttributes) {
        if (variantAttributes == null) return new ArrayList<>();
        return variantAttributes.stream()
                .map(attr -> ProductAttributeDocument.builder()
                        .attributeName(attr.getProductAttributeValue().getProductAttribute().getDisplayName() != null ?
                                attr.getProductAttributeValue().getProductAttribute().getDisplayName() :
                                attr.getProductAttributeValue().getProductAttribute().getName())
                        .attributeValue(attr.getProductAttributeValue().getDisplayValue() != null ?
                                attr.getProductAttributeValue().getDisplayValue() :
                                attr.getProductAttributeValue().getValue())
                        .build())
                .collect(Collectors.toList());
    }

    // Các phương thức map Page
    public Page<ProductResponse> toDtoPageFromDocumentPage(Page<ProductDocument> productDocumentPage) {
        return productDocumentPage.map(this::toProductResponse);
    }

    public Page<ProductResponse> toDtoPageFromEntityPage(Page<Product> productPage) {
        return productPage.map(this::toProductResponse);
    }
}
