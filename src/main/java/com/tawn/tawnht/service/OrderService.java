package com.tawn.tawnht.service;

import com.tawn.tawnht.dto.request.OrderCreationRequest;
import com.tawn.tawnht.dto.request.OrderItemRequest;
import com.tawn.tawnht.dto.response.OrderResponse;
import com.tawn.tawnht.dto.response.PageResponse;
import com.tawn.tawnht.dto.response.SubOrderResponse;
import com.tawn.tawnht.dto.response.UserAddressResponse;
import com.tawn.tawnht.entity.*;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.mapper.OrderMapper;
import com.tawn.tawnht.repository.jpa.*;
import com.tawn.tawnht.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class OrderService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    ProductVariantRepository productVariantRepository;
    ProductAttributeValueRepository productAttributeValueRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    CartRepository cartRepository;
    SubOrderRepository subOrderRepository;
    UserAddressRepository userAddressRepository;
    SellerRepository sellerRepository;
    OrderMapper orderMapper;


    public OrderResponse createOrder(OrderCreationRequest request){
        // 1. Xác thực người dùng
        String email = SecurityUtils.getCurrentUserLogin().get();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Kiểm tra địa chỉ giao hàng
        UserAddress address = userAddressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Lấy danh sách mặt hàng
        List<OrderItemRequest> items;
        if (request.isFromCart()) {
            Cart cart = cartRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            items = cart.getCartItems().stream()
                    .flatMap(item -> item.getVariantQuantities().entrySet().stream()
                            .map(entry -> {
                                ProductVariant variant = entry.getKey();
                                Integer quantity = entry.getValue();
                                OrderItemRequest req = new OrderItemRequest();
                                req.setProductId(variant.getProduct().getId());
                                req.setAttributeValueIds(variant.getProductVariantAttributes().stream()
                                        .map(attr -> attr.getProductAttributeValue().getId())
                                        .collect(Collectors.toList()));
                                req.setQuantity(quantity);
                                return req;
                            }))
                    .collect(Collectors.toList());
        } else {
            items = request.getItems();
        }

        // 4. Kiểm tra điều kiện
        List<ItemInfo> itemInfos = new ArrayList<>();
        for (OrderItemRequest item : items) {
            Long variantId = getVariantId(item.getProductId(), item.getAttributeValueIds());
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            if (variant.getStock() < item.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
            if (!variant.getProduct().isActive()) {
                throw new AppException(ErrorCode.PRODUCT_NOT_ACTIVE);
            }
            itemInfos.add(new ItemInfo(variantId, variant.getProduct().getSeller().getId(), item.getQuantity(), variant.getPrice()));
        }

        // 5. Nhóm sản phẩm theo shop
        Map<Long, List<ItemInfo>> itemsBySeller = itemInfos.stream()
                .collect(Collectors.groupingBy(ItemInfo::getSellerId));

        // 6. Tạo đơn hàng tổng
        BigDecimal totalAmount = calculateTotalAmount(itemInfos);
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .shippingFee(BigDecimal.ZERO) // Tính sau
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .couponCode(request.getCouponCode())
                .status("pending")
                .paymentMethod(request.getPaymentMethod())
                .userAddress(address)
                .note(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);

        // 7. Tạo đơn hàng con và mặt hàng
        for (Map.Entry<Long, List<ItemInfo>> entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<ItemInfo> sellerItems = entry.getValue();

            // Tạo SubOrder
            SubOrder subOrder = SubOrder.builder()
                    .order(order)
                    .seller(sellerRepository.findById(sellerId)
                            .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND)))
                    .status("pending")
                    .createdAt(LocalDateTime.now())
                    .build();
            subOrderRepository.save(subOrder);

            // Tạo OrderItem
            for (ItemInfo item : sellerItems) {
                ProductVariant variant = productVariantRepository.findById(item.getVariantId()).get();
                OrderItem orderItem = OrderItem.builder()
                        .subOrder(subOrder)
                        .productVariant(variant)
                        .quantity(item.getQuantity())
                        .price(variant.getPrice())
                        .build();
                orderItemRepository.save(orderItem);

                // Cập nhật tồn kho
                variant.setStock(variant.getStock() - item.getQuantity());
                productVariantRepository.save(variant);
            }
        }

//        // 8. Xử lý mã giảm giá
//        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
//            Coupon coupon = couponRepository.findByCode(request.getCouponCode())
//                    .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
//            if (coupon.getExpiresAt().isBefore(LocalDateTime.now()) || coupon.getUsageCount() >= coupon.getUsageLimit()) {
//                throw new AppException(ErrorCode.COUPON_INVALID);
//            }
//            BigDecimal discount = calculateDiscount(coupon, totalAmount);
//            order.setDiscountAmount(discount);
//            order.setTotalAmount(totalAmount.subtract(discount));
//            coupon.setUsageCount(coupon.getUsageCount() + 1);
//            couponRepository.save(coupon);
//            orderRepository.save(order);
//        }

        // 9. Xóa giỏ hàng nếu từ cart
        if (request.isFromCart()) {
            Cart cart = cartRepository.findByUserId(user.getId()).get();
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }

        // 10. Trả về response
        return orderMapper.toOrderResponse(order);

    }
    public Long getVariantId(Long productId, List<Long> attributeValueIds) {

        // 2. Kiểm tra các attribute_value_id tồn tại
        List<Long> validAttributeValueIds = productAttributeValueRepository
                .findAllById(attributeValueIds)
                .stream()
                .map(ProductAttributeValue::getId)
                .collect(Collectors.toList());


        // 3. Tìm variant_id
        Long attributeValueCount = (long) attributeValueIds.size();
        Long variantId = productVariantRepository
                .findVariantIdByProductIdAndAttributeValueIds(productId, attributeValueIds, attributeValueCount)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        return variantId;
    }
    private BigDecimal calculateTotalAmount(List<ItemInfo> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

//    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal totalAmount) {
//        if (coupon.getDiscountType().equals("percent")) {
//            return totalAmount.multiply(BigDecimal.valueOf(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100)));
//        } else if (coupon.getDiscountType().equals("fixed")) {
//            return BigDecimal.valueOf(coupon.getDiscountValue());
//        }
//        return BigDecimal.ZERO;
//    }
    // Lớp phụ trợ
    @Data
    private static class ItemInfo {
        private Long variantId;
        private Long sellerId;
        private int quantity;
        private BigDecimal price;

        public ItemInfo(Long variantId, Long sellerId, int quantity, BigDecimal price) {
            this.variantId = variantId;
            this.sellerId = sellerId;
            this.quantity = quantity;
            this.price = price;
        }
    }
    public PageResponse<SubOrderResponse> getAllOrderBySeller(int page, int size){
        String email=SecurityUtils.getCurrentUserLogin().orElseThrow(
                ()->new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        Seller seller=user.getSeller();
        Sort sort= Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<SubOrder> subOrders=subOrderRepository.findAllBySeller(pageable,seller);
        List<SubOrderResponse> subOrderResponses=subOrders.getContent().stream()
                .map(orderMapper::toSubOrderResponse).toList();
        return PageResponse.<SubOrderResponse>builder()
                .currentPage(page)
                .data(subOrderResponses)
                .pageSize(pageable.getPageSize())
                .totalElements(subOrders.getTotalElements())
                .totalPages(subOrders.getTotalPages())
                .build();
    }
}
