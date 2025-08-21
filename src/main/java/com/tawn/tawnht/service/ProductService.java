package com.tawn.tawnht.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tawn.tawnht.document.ProductDocument;
import com.tawn.tawnht.document.ProductElasticsearchRepository;
import com.tawn.tawnht.dto.request.AttributeRequest;
import com.tawn.tawnht.dto.request.AttributeValueRequest;
import com.tawn.tawnht.dto.request.ProductCreationRequest;
import com.tawn.tawnht.dto.request.ProductVariantRequest;
import com.tawn.tawnht.dto.response.*;
import com.tawn.tawnht.entity.*;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.mapper.ProductMapper;
import com.tawn.tawnht.repository.jpa.*;
import com.tawn.tawnht.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    UserRepository userRepository;
    SellerRepository sellerRepository;
    ProductAttributeRepository productAttributeRepository;
    ProductAttributeValueRepository productAttributeValueRepository;
    ProductMapper productMapper;
    ProductElasticsearchRepository productElasticsearchRepository;

    @Transactional
    public ProductResponse createProduct(ProductCreationRequest request) {
        // Kiểm tra dữ liệu đầu vào
        //        if (request.getName() == null || request.getName().isBlank()) {
        //            throw new AppException(ErrorCode.INVALID_PRODUCT_NAME);
        //        }
        //        if (request.getImages() == null || request.getImages().isEmpty()) {
        //            throw new AppException(ErrorCode.NO_IMAGES_PROVIDED);
        //        }
        //        if (productRepository.existsBySlug(request.getSlug())) {
        //            throw new AppException(ErrorCode.SLUG_ALREADY_EXISTS);
        //        }

        String email =
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        Seller seller = sellerRepository
                .findSellerByUser(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        Product product = Product.builder()
                .createdAt(LocalDateTime.now())
                .name(request.getName())
                .slug(request.getSlug())
                .category(category)
                .description(request.getDescription())
                .metaTitle(request.getMetaTitle())
                .seller(seller)
                .isActive(true)
                .coverImage(request.getCoverImage())
                .metaDescription(request.getMetaDescription())
                .warrantyInfo(request.getWarrantyInfo())
                .build();

        Set<ProductImage> productImages = new HashSet<>();
        for (int i = 0; i < request.getImages().size(); i++) {
            ProductImage productImage = ProductImage.builder()
                    .createdAt(LocalDateTime.now())
                    .imageUrl(request.getImages().get(i))
                    .displayOrder(i)
                    .product(product)
                    .build();
            productImages.add(productImage);
        }
        product.setImages(productImages);

        Set<ProductVariant> productVariants = new HashSet<>();
        for (ProductVariantRequest variantRequest : request.getVariants()) {
            ProductVariant productVariant = ProductVariant.builder()
                    .sku(variantRequest.getSku())
                    .product(product)
                    .isActive(true)
                    .image(variantRequest.getImage())
                    .price(variantRequest.getPrice())
                    .stock(variantRequest.getStock())
                    .build();

            Set<ProductVariantAttribute> variantAttributes = new HashSet<>();
            for (AttributeRequest attrRequest : variantRequest.getAttributes()) {
                ProductAttribute productAttribute = productAttributeRepository
                        .findByName(attrRequest.getName())
                        .orElseGet(() -> {
                            ProductAttribute newAttr = ProductAttribute.builder()
                                    .name(attrRequest.getName())
                                    .build();
                            return productAttributeRepository.save(newAttr);
                        });

                for (AttributeValueRequest valueRequest : attrRequest.getAttributeValue()) {
                    ProductAttributeValue attributeValue = productAttributeValueRepository
                            .findByProductAttributeAndValue(productAttribute, valueRequest.getValue())
                            .orElseGet(() -> {
                                ProductAttributeValue newValue = ProductAttributeValue.builder()
                                        .productAttribute(productAttribute)
                                        .value(valueRequest.getValue())
                                        .build();
                                return productAttributeValueRepository.save(newValue);
                            });

                    ProductVariantAttribute variantAttribute = ProductVariantAttribute.builder()
                            .productVariant(productVariant)
                            .productAttributeValue(attributeValue)
                            .build();
                    variantAttributes.add(variantAttribute);
                }
            }
            productVariant.setProductVariantAttributes(variantAttributes);
            productVariants.add(productVariant);
        }
        product.setProductVariants(productVariants);

        product = productRepository.save(product);

        List<ProductVariantResponse> productVariantResponses = new ArrayList<>();
        for (ProductVariant productVariant : product.getProductVariants()) {
            List<AttributeResponse> attributeResponses = new ArrayList<>();
            for (ProductVariantAttribute pva : productVariant.getProductVariantAttributes()) {
                ProductAttributeValue pav = pva.getProductAttributeValue();
                ProductAttribute pa = pav.getProductAttribute();
                AttributeResponse attributeResponse = AttributeResponse.builder()
                        .attributeId(pa.getId())
                        .attributeName(pa.getName())
                        .attributeValue(List.of(AttributeValueResponse.builder()
                                .attributeValueId(pav.getId())
                                .value(pav.getValue())
                                .build()))
                        .build();
                attributeResponses.add(attributeResponse);
            }

            ProductVariantResponse variantResponse = ProductVariantResponse.builder()
                    .id(productVariant.getId())
                    .sku(productVariant.getSku())
                    .stock(productVariant.getStock())
                    .productId(product.getId())
                    .price(productVariant.getPrice())
                    .image(productVariant.getImage())
                    .attributes(attributeResponses)
                    .build();
            productVariantResponses.add(variantResponse);
        }

        List<ProductImageResponse> productImageResponses = product.getImages().stream()
                .map(image -> ProductImageResponse.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .displayOrder(image.getDisplayOrder())
                        .createdAt(image.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        ProductDocument productDocument = productMapper.toDocument(product);
        productElasticsearchRepository.save(productDocument);
        log.info("productDocument: {}", productDocument);
        // Tạo response
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .categoryId(request.getCategoryId())
                .description(product.getDescription())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .coverImage(product.getCoverImage())
                .warrantyInfo(product.getWarrantyInfo())
                .sellerId(product.getSeller().getId())
                .productVariants(productVariantResponses)
                .images(productImageResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product =
                productRepository.findProductById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        List<ProductVariantResponse> productVariantResponses = new ArrayList<>();
        for (ProductVariant productVariant : product.getProductVariants()) {
            List<AttributeResponse> attributeResponses = new ArrayList<>();
            for (ProductVariantAttribute pva : productVariant.getProductVariantAttributes()) {
                ProductAttributeValue pav = pva.getProductAttributeValue();
                ProductAttribute pa = pav.getProductAttribute();
                AttributeResponse attributeResponse = AttributeResponse.builder()
                        .attributeId(pa.getId())
                        .attributeName(pa.getName())
                        .attributeValue(List.of(AttributeValueResponse.builder()
                                .attributeValueId(pav.getId())
                                .value(pav.getValue())
                                .build()))
                        .build();
                attributeResponses.add(attributeResponse);
            }

            ProductVariantResponse productVariantResponse = ProductVariantResponse.builder()
                    .id(productVariant.getId())
                    .sku(productVariant.getSku())
                    .productId(product.getId())
                    .image(productVariant.getImage())
                    .price(productVariant.getPrice())
                    .stock(productVariant.getStock())
                    .attributes(attributeResponses)
                    .build();
            productVariantResponses.add(productVariantResponse);
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .coverImage(product.getCoverImage())
                .warrantyInfo(product.getWarrantyInfo())
                .categoryId(product.getCategory().getId())
                .sellerId(product.getSeller().getId())
                .images(product.getImages().stream()
                        .map(image -> ProductImageResponse.builder()
                                .id(image.getId())
                                .imageUrl(image.getImageUrl())
                                .displayOrder(image.getDisplayOrder())
                                .build())
                        .collect(Collectors.toList()))
                .productVariants(productVariantResponses)
                .build();
    }

    public PageResponse<ProductResponse> getAllProduct(Specification<Product> spec, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Product> products =
                (spec != null) ? productRepository.findAll(spec, pageable) : productRepository.findAll(pageable);

        List<ProductResponse> productResponses = products.getContent().stream()
                .map(product -> {
                    if (product == null) return null;
                    return productMapper.toProductResponse(product); // Sử dụng ProductMapper
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .currentPage(products.getNumber() + 1) // Sử dụng 1-based index
                .pageSize(pageable.getPageSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .data(productResponses)
                .build();
    }

    public PageResponse<ProductResponse> getByCategory(
            Long categoryId, Specification<Product> spec, int page, int size) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Product> products = productRepository.findAllByCategory(pageable, category); // Giả định phương thức này
        List<ProductResponse> productResponses = products.getContent().stream()
                .map(productMapper::toProductResponse)
                .toList();

        return PageResponse.<ProductResponse>builder()
                .currentPage(products.getNumber() + 1) // Sử dụng 1-based index
                .pageSize(pageable.getPageSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .data(productResponses)
                .build();
    }

    public PageResponse<ProductResponse> getFiveProduct(Specification<Product> spec, int page, int size) {
        page = 1;
        size = 5;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt", "purchase");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Product> products =
                (spec != null) ? productRepository.findAll(spec, pageable) : productRepository.findAll(pageable);

        List<ProductResponse> productResponses = products.getContent().stream()
                .map(product -> {
                    if (product == null) return null;
                    return productMapper.toProductResponse(product); // Sử dụng ProductMapper
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .currentPage(products.getNumber() + 1) // Sử dụng 1-based index
                .pageSize(pageable.getPageSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .data(productResponses)
                .build();
    }

    public PageResponse<ProductResponse> getAllBySeller(Specification<Product> spec, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        User user = userRepository
                .findByEmail(SecurityUtils.getCurrentUserLogin().get())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Seller seller = user.getSeller();
        log.info("seller id:" + seller.getId());
        Page<Product> products = productRepository.findAllBySeller(pageable, seller); // Giả định phương thức này
        List<ProductResponse> productResponses = products.getContent().stream()
                .map(productMapper::toProductResponse)
                .toList();

        return PageResponse.<ProductResponse>builder()
                .currentPage(products.getNumber() + 1) // Sử dụng 1-based index
                .pageSize(pageable.getPageSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .data(productResponses)
                .build();
    }

    public PageResponse<ProductResponse> search(String keyword, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "purchase", "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<ProductDocument> productDocuments =
                productElasticsearchRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        keyword, keyword, pageable); // Giả định phương thức này
        List<ProductResponse> productResponses = productDocuments.getContent().stream()
                .map(productMapper::toProductResponse)
                .toList();

        return PageResponse.<ProductResponse>builder()
                .currentPage(productDocuments.getNumber() + 1) // Sử dụng 1-based index
                .pageSize(pageable.getPageSize())
                .totalElements(productDocuments.getTotalElements())
                .totalPages(productDocuments.getTotalPages())
                .data(productResponses)
                .build();
    }

    public String deleteProduct(Long id) {
        productRepository.deleteById(id);
        return "deleted";
    }
}
