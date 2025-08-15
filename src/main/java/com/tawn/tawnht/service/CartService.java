package com.tawn.tawnht.service;

import com.tawn.tawnht.dto.request.AddToCartReq;
import com.tawn.tawnht.dto.request.AddressCreationRequest;
import com.tawn.tawnht.dto.response.*;
import com.tawn.tawnht.entity.*;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.mapper.OrderMapper;
import com.tawn.tawnht.repository.jpa.*;
import com.tawn.tawnht.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class CartService {
     CartRepository cartRepository;
    ProductVariantRepository variantRepository;
     ProductVariantAttributeRepository productVariantAttributeRepository;
     UserRepository userRepository;
     CartItemRepository cartItemRepository;

    @Transactional
    public CartResponse addToCart(AddToCartReq items) {
        User user = userRepository.findByEmail(SecurityUtils.getCurrentUserLogin().get())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String userId = user.getId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        AddToCartReq itemRequest=items;
            List<Long> attributeValueIds = itemRequest.getAttributeValueIds();
            Integer quantity = itemRequest.getQuantity();
            Long productId = itemRequest.getProductId(); // Lấy productId

            log.info("Processing item with productId: {}, attributeValueIds: {}", productId, attributeValueIds);

            ProductVariant variant = findMatchingVariant(productId, attributeValueIds); // Sử dụng productId
            if (variant == null) {
                log.warn("No matching variant found for productId: {}, attributeValueIds: {}", productId, attributeValueIds);
                throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
            }

            if (quantity > variant.getStock()) {
                log.warn("Insufficient stock for variant ID: {}, requested: {}, available: {}", variant.getId(), quantity, variant.getStock());
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            CartItem cartItem = cart.getCartItems().stream()
                    .filter(item -> item.getVariantQuantities().containsKey(variant))
                    .findFirst()
                    .orElseGet(() -> {
                        CartItem newItem = new CartItem();
                        newItem.setCart(cart);
                        newItem.setAddedAt(LocalDateTime.now());
                        cart.getCartItems().add(newItem); // An toàn vì cartItems đã khởi tạo
                        return newItem;
                    });

            cartItem.getVariantQuantities().put(variant, quantity);


        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    private ProductVariant findMatchingVariant(Long productId, List<Long> attributeValueIds) {
        // 1. Lấy tất cả các biến thể của sản phẩm, đảm bảo các thuộc tính được load
        // RẤT QUAN TRỌNG: Đảm bảo findByProductIdWithAttributes(productId) tải eager các ProductVariantAttributes và ProductAttributeValue
        List<ProductVariant> variants = variantRepository.findByProductIdWithAttributes(productId);

        if (variants == null || variants.isEmpty()) {
            log.warn("No variants found for productId: {}", productId);
            return null;
        }

        // Chuyển List<Long> attributeValueIds thành Set<Long> để so sánh hiệu quả hơn
        Set<Long> requestedAttributeValueSet = new HashSet<>(attributeValueIds);

        // 2. Duyệt qua từng biến thể để tìm biến thể khớp
        for (ProductVariant variant : variants) {
            // Lấy tất cả các ProductAttributeValue IDs của biến thể hiện tại
            Set<Long> variantAttributeValueSet = variant.getProductVariantAttributes().stream()
                    .map(ProductVariantAttribute::getProductAttributeValue) // Lấy ProductAttributeValue
                    .map(ProductAttributeValue::getId) // Lấy ID của ProductAttributeValue
                    .collect(Collectors.toSet());

            // Kiểm tra xem tập hợp các thuộc tính của biến thể hiện tại
            // có khớp HOÀN TOÀN với tập hợp các thuộc tính yêu cầu hay không.
            // Điều kiện:
            // a) Kích thước của hai tập hợp phải bằng nhau (số lượng thuộc tính khớp)
            // b) Tập hợp các thuộc tính của biến thể phải chứa TẤT CẢ các thuộc tính yêu cầu
            if (variantAttributeValueSet.size() == requestedAttributeValueSet.size() &&
                    variantAttributeValueSet.containsAll(requestedAttributeValueSet)) {

                // Nếu tìm thấy biến thể khớp, trả về nó
                log.info("Found matching variant ID: {} for productId: {}, attributeValueIds: {}", variant.getId(), productId, attributeValueIds);
                return variant;
            }
        }

        // Nếu không tìm thấy biến thể nào khớp sau khi duyệt qua tất cả
        log.warn("No matching variant found for productId: {}, attributeValueIds: {}", productId, attributeValueIds);
        return null;
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());
        response.setCartItems(cart.getCartItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        log.info("Mapping CartItem with id: {}", item.getId());
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setAddedAt(item.getAddedAt());

        response.setVariants(item.getVariantQuantities().entrySet().stream()
                .map(entry -> {
                    ProductVariant variant = entry.getKey();
                    Integer quantity = entry.getValue();
                    log.info("Mapping variant {} with quantity {}", variant.getId(), quantity);

                    CartItemResponse.VariantQuantity variantQty = new CartItemResponse.VariantQuantity();
                    variantQty.setVariantId(variant.getId());
                    variantQty.setProductId(variant.getProduct().getId());
                    variantQty.setProductName(variant.getProduct().getName());
                    variantQty.setPrice(variant.getPrice());
                    variantQty.setStock(variant.getStock());
                    variantQty.setImage(variant.getImage());
                    variantQty.setQuantity(quantity);
                    variantQty.setAttributes(variant.getProductVariantAttributes().stream()
                            .map(attr -> {
                                AttributeResponse attrRes = new AttributeResponse();
                                ProductAttributeValue selectedValue = attr.getProductAttributeValue();
                                attrRes.setAttributeId(selectedValue.getProductAttribute().getId());
                                attrRes.setAttributeName(selectedValue.getProductAttribute().getName());

                                // Chỉ lấy giá trị đã chọn, không lấy tất cả từ ProductAttributeValues
                                AttributeValueResponse valueRes = new AttributeValueResponse();
                                valueRes.setAttributeValueId(selectedValue.getId());
                                valueRes.setValue(selectedValue.getValue());
                                valueRes.setDisplayValue(selectedValue.getDisplayValue() != null ? selectedValue.getDisplayValue() : selectedValue.getValue());
                                attrRes.setAttributeValue(List.of(valueRes));

                                return attrRes;
                            })
                            .collect(Collectors.toList()));
                    return variantQty;
                })
                .collect(Collectors.toList()));

        log.info("Mapped CartItemResponse with {} variants", response.getVariants().size());
        return response;
    }

    public List<CartItemResponse> getAllItem() {
        User user = userRepository.findByEmail(SecurityUtils.getCurrentUserLogin().get())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());
        log.info("GET CART {}",cartItems);
        return cartItems.stream().map(this::mapToCartItemResponse).collect(Collectors.toList());
    }
}
