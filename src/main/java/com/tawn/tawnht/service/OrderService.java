package com.tawn.tawnht.service;

import com.tawn.tawnht.dto.request.OrderCreationRequest;
import com.tawn.tawnht.dto.request.OrderItemRequest;
import com.tawn.tawnht.dto.request.SetStatusOrderReq;
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
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
        String email = SecurityUtils.getCurrentUserLogin().get();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserAddress address = userAddressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        log.info("is from cart: "+request.isFromCart()+"");
        List<OrderItemRequest> items;
        if (request.isFromCart()) {
            Cart cart = cartRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            log.info("cart id: "+cart.getId()+"");
            items = cart.getCartItems().stream()
                    .flatMap(item -> item.getVariantQuantities().entrySet().stream()
                            .map(entry -> {
                                ProductVariant variant = entry.getKey();
                                log.info("product var   iant id: "+variant.getId()+"");
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

        List<ItemInfo> itemInfos = new ArrayList<>();
        for (OrderItemRequest item : items) {
            Long variantId = getVariantId(item.getProductId(), item.getAttributeValueIds());
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            if (variant.getStock() < item.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            itemInfos.add(new ItemInfo(variantId, variant.getProduct().getSeller().getId(), item.getQuantity(), variant.getPrice()));
        }


        Map<Long, List<ItemInfo>> itemsBySeller = itemInfos.stream()
                .collect(Collectors.groupingBy(ItemInfo::getSellerId));

        BigDecimal totalAmount = calculateTotalAmount(itemInfos);
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .shippingFee(BigDecimal.ZERO) // Tính sau
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .couponCode(request.getCouponCode())
                .status("init")
                .paymentMethod(request.getPaymentMethod())
                .userAddress(address)
                .note(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();
        order=orderRepository.save(order);
        log.info("order id: "+order.getId());
        List<SubOrder> subOrders=new ArrayList<>();
        Set<OrderItem> orderItems=new HashSet<>();
        for (Map.Entry<Long, List<ItemInfo>> entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            log.info("seller id: "+sellerId+"");
            List<ItemInfo> sellerItems = entry.getValue();

            SubOrder subOrder = SubOrder.builder()
                    .order(order)
                    .seller(sellerRepository.findById(sellerId)
                            .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND)))
                    .status("pending")
                    .createdAt(LocalDateTime.now())
                    .build();
            subOrder=subOrderRepository.save(subOrder);

            log.info("suborderId "+subOrder.getId()+"");

            for (ItemInfo item : sellerItems) {
                ProductVariant variant = productVariantRepository.findById(item.getVariantId()).get();
                OrderItem orderItem = OrderItem.builder()
                        .subOrder(subOrder)
                        .productVariant(variant)
                        .quantity(item.getQuantity())
                        .price(variant.getPrice())
                        .build();
                log.info("variant price: "+variant.getPrice());
               orderItem= orderItemRepository.save(orderItem);
               orderItems.add(orderItem);
                variant.setStock(variant.getStock() - item.getQuantity());
               variant= productVariantRepository.save(variant);
            }
            subOrder.setOrderItems(orderItems);
            subOrders.add(subOrder);
        }
        order.setSubOrders(subOrders);
//

        if (request.isFromCart()) {
            Cart cart = cartRepository.findByUserId(user.getId()).get();
            cart.getCartItems().clear();
            cart=cartRepository.save(cart);
        }
        log.info("totalAmount: "+totalAmount);
        return orderMapper.toOrderResponse(order);

    }
    public Long getVariantId(Long productId, List<Long> attributeValueIds) {

        List<Long> validAttributeValueIds = productAttributeValueRepository
                .findAllById(attributeValueIds)
                .stream()
                .map(ProductAttributeValue::getId)
                .collect(Collectors.toList());


        Long attributeValueCount = (long) attributeValueIds.size();
        Long variantId = productVariantRepository
                .findVariantIdByProductIdAndAttributeValueIds(productId, attributeValueIds, attributeValueCount)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        return variantId;
    }
    private BigDecimal calculateTotalAmount(List<ItemInfo> items) {
        log.info("variant price:" +items.get(0).getPrice()+"");
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
    public PageResponse<SubOrderResponse> getAllOrderBySeller(Specification<Order> spec,int page, int size){
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
    @Transactional
    public Void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra trạng thái
        if (!"PENDING".equals(order.getStatus()) && !"CANCELLED".equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_DELETED);
        }

        // Lấy tất cả SubOrder
        List<SubOrder> subOrders = order.getSubOrders();
        for (SubOrder subOrder : subOrders) {
            // Hoàn lại stock cho ProductVariant
            subOrder.getOrderItems().forEach(item -> {
                ProductVariant variant = item.getProductVariant();
                variant.setStock(variant.getStock() + item.getQuantity());
                productVariantRepository.save(variant);
            });

            // Xóa OrderItem
            orderItemRepository.deleteBySubOrderId(subOrder.getId());
        }

        // Xóa SubOrder
        subOrderRepository.deleteByOrderId(orderId);

        // Xóa Order
        orderRepository.delete(order);
    }
    public OrderResponse startOrder(Long orderId){
       Order order=orderRepository.findById(orderId)
                .orElseThrow(()->new AppException(ErrorCode.ORDER_NOT_FOUND));
       order.setStatus("pending");
       List<SubOrder> subOrders=order.getSubOrders();
       for(SubOrder subOrder:subOrders){
           Set<OrderItem> orderItems=subOrder.getOrderItems();
           for (OrderItem orderItem:orderItems){
               ProductVariant productVariant=orderItem.getProductVariant();
               Product product=productVariant.getProduct();
               int purchase= product.getPurchase();;
               product.setPurchase(purchase+orderItem.getQuantity());
               productRepository.save(product);
           }
       }
       order=orderRepository.save(order);
       return orderMapper.toOrderResponse(order);
    }

    public SubOrderResponse setStatus(SetStatusOrderReq req){
        SubOrder subOrder=subOrderRepository.findById(req.getSubOrderId())
                .orElseThrow(()->new AppException(ErrorCode.ORDER_NOT_FOUND));
        subOrder.setStatus(req.getStatus());
        return orderMapper.toSubOrderResponse(subOrderRepository.save(subOrder));
    }
}
