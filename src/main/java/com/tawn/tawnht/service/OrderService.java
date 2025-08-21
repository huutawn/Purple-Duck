package com.tawn.tawnht.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.google.zxing.WriterException;
import com.tawn.tawnht.utils.CodeUtil;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.tawn.tawnht.document.ProductDocument;
import com.tawn.tawnht.document.ProductElasticsearchRepository;
import com.tawn.tawnht.dto.request.OrderCreationRequest;
import com.tawn.tawnht.dto.request.OrderItemRequest;
import com.tawn.tawnht.dto.request.SetStatusOrderReq;
import com.tawn.tawnht.dto.request.StartOrderReq;
import com.tawn.tawnht.dto.response.OrderResponse;
import com.tawn.tawnht.dto.response.PageResponse;
import com.tawn.tawnht.dto.response.SubOrderResponse;
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

@Service
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
    CloudinaryService cloudinaryService;
    OrderMapper orderMapper;
    ProductElasticsearchRepository productElasticsearchRepository;
    QRService qrService;

    public OrderResponse createOrder(OrderCreationRequest request) {
        String email = SecurityUtils.getCurrentUserLogin().get();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserAddress userAddress = UserAddress.builder().status("non").user(user).build();
        userAddressRepository.saveAndFlush(userAddress);
        Set<UserAddress> userAddresses = new HashSet<>();
        userAddresses.add(userAddress);
        user.setUserAddresses(userAddresses);
        userRepository.saveAndFlush(user);

        log.info("is from cart: " + request.isFromCart() + "");

        // LUÔN LUÔN lấy danh sách các item từ request.getItems()
        List<OrderItemRequest> itemsToProcess = request.getItems();

        // Thêm kiểm tra an toàn: đảm bảo danh sách item không rỗng
        if (itemsToProcess == null || itemsToProcess.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_ITEMS_EMPTY); // Cần định nghĩa ErrorCode này
        }

        List<ItemInfo> itemInfos = new ArrayList<>();
        for (OrderItemRequest item : itemsToProcess) {
            Long variantId = getVariantId(item.getProductId(), item.getAttributeValueIds());
            ProductVariant variant = productVariantRepository
                    .findById(variantId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            if (variant.getStock() < item.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            itemInfos.add(new ItemInfo(
                    variantId, variant.getProduct().getSeller().getId(), item.getQuantity(), variant.getPrice()));
        }

        Map<Long, List<ItemInfo>> itemsBySeller =
                itemInfos.stream().collect(Collectors.groupingBy(ItemInfo::getSellerId));

        BigDecimal totalAmount = calculateTotalAmount(itemInfos);

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .shippingFee(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .couponCode(request.getCouponCode())
                .status("init")
                .paymentMethod(request.getPaymentMethod())
                .userAddress(userAddress)
                .note(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);
        log.info("order id: " + order.getId());

        List<SubOrder> subOrders = new ArrayList<>();
        Set<OrderItem> orderItems = new HashSet<>(); // Đây là Set của OrderItem, không phải CartItem
        for (Map.Entry<Long, List<ItemInfo>> entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            log.info("seller id: " + sellerId + "");
            List<ItemInfo> sellerItems = entry.getValue();

            SubOrder subOrder = SubOrder.builder()
                    .order(order)
                    .seller(sellerRepository
                            .findById(sellerId)
                            .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND)))
                    .status("init")
                    .createdAt(LocalDateTime.now())
                    .build();
            subOrder = subOrderRepository.save(subOrder);

            log.info("suborderId " + subOrder.getId() + "");

            for (ItemInfo item : sellerItems) {
                ProductVariant variant =
                        productVariantRepository.findById(item.getVariantId()).get();
                OrderItem orderItem = OrderItem.builder()
                        .subOrder(subOrder)
                        .productVariant(variant)
                        .quantity(item.getQuantity())
                        .price(variant.getPrice())
                        .build();
                log.info("variant price: " + variant.getPrice());
                orderItem = orderItemRepository.save(orderItem);
                orderItems.add(orderItem);
                variant.setStock(variant.getStock() - item.getQuantity());
                variant = productVariantRepository.save(variant);
            }
            subOrder.setOrderItems(orderItems);
            subOrders.add(subOrder);
        }
        order.setSubOrders(subOrders);

        // Logic mới: Xóa các CartItem đã chọn khỏi giỏ hàng nếu fromCart là true
        if (request.isFromCart()) {
            Cart cart = cartRepository
                    .findByUserId(user.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

            // 1. Lấy tất cả ProductVariant IDs cần xóa khỏi giỏ hàng
            Set<Long> variantIdsToRemove = itemsToProcess.stream()
                    .map(itemRequest -> getVariantId(itemRequest.getProductId(), itemRequest.getAttributeValueIds()))
                    .collect(Collectors.toSet());

            // 2. Duyệt qua từng CartItem trong giỏ hàng của người dùng
            // Sử dụng Iterator để có thể xóa phần tử an toàn trong khi lặp
            Iterator<CartItem> cartItemIterator = cart.getCartItems().iterator();
            while (cartItemIterator.hasNext()) {
                CartItem cartItem = cartItemIterator.next();
                Map<ProductVariant, Integer> currentVariantQuantities = cartItem.getVariantQuantities();

                // Tạo một Map mới để chứa các biến thể còn lại sau khi xóa
                Map<ProductVariant, Integer> updatedVariantQuantities = new HashMap<>();

                // Duyệt qua từng cặp ProductVariant-quantity trong CartItem hiện tại
                for (Map.Entry<ProductVariant, Integer> entry : currentVariantQuantities.entrySet()) {
                    ProductVariant variantInCart = entry.getKey();
                    Integer quantityInCart = entry.getValue();

                    // Nếu biến thể này KHÔNG nằm trong danh sách cần xóa, giữ lại nó
                    if (!variantIdsToRemove.contains(variantInCart.getId())) {
                        updatedVariantQuantities.put(variantInCart, quantityInCart);
                    }
                }

                // Cập nhật lại Map variantQuantities của CartItem
                cartItem.setVariantQuantities(updatedVariantQuantities);

                // Nếu sau khi cập nhật, CartItem này không còn biến thể nào, xóa CartItem đó khỏi giỏ hàng
                if (updatedVariantQuantities.isEmpty()) {
                    cartItemIterator.remove(); // Xóa CartItem khỏi Set cartItems của Cart
                    // Nếu CartItem là một entity riêng biệt và có thể cần xóa khỏi DB
                    // cartItemRepository.delete(cartItem); // Chỉ xóa nếu CartItem không tự động bị xóa khi không còn
                    // liên kết
                }
            }

            // Lưu lại giỏ hàng đã cập nhật (với các CartItem đã sửa đổi hoặc đã xóa)
            cartRepository.save(cart);
        }
        log.info("totalAmount: " + totalAmount);
        return orderMapper.toOrderResponse(order);
    }

    public Long getVariantId(Long productId, List<Long> attributeValueIds) {

        List<Long> validAttributeValueIds = productAttributeValueRepository.findAllById(attributeValueIds).stream()
                .map(ProductAttributeValue::getId)
                .collect(Collectors.toList());

        Long attributeValueCount = (long) attributeValueIds.size();
        Long variantId = productVariantRepository
                .findVariantIdByProductIdAndAttributeValueIds(productId, attributeValueIds, attributeValueCount)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        return variantId;
    }

    private BigDecimal calculateTotalAmount(List<ItemInfo> items) {
        log.info("variant price:" + items.get(0).getPrice() + "");
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal totalAmount) {
    //        if (coupon.getDiscountType().equals("percent")) {
    //            return
    // totalAmount.multiply(BigDecimal.valueOf(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100)));
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

    public PageResponse<SubOrderResponse> getAllOrderBySeller(Specification<Order> spec, int page, int size) {
        String email =
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Seller seller = user.getSeller();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<SubOrder> subOrders = subOrderRepository.findAllBySeller(pageable, seller);
        List<SubOrderResponse> subOrderResponses = subOrders.getContent().stream()
                .map(orderMapper::toSubOrderResponse)
                .toList();
        return PageResponse.<SubOrderResponse>builder()
                .currentPage(page)
                .data(subOrderResponses)
                .pageSize(pageable.getPageSize())
                .totalElements(subOrders.getTotalElements())
                .totalPages(subOrders.getTotalPages())
                .build();
    }

    public PageResponse<OrderResponse> getAll(Specification<Order> spec, int page, int size) {
        String email =
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Order> orders = orderRepository.findAllByUser(user, pageable);
        List<OrderResponse> orderResponses =
                orders.getContent().stream().map(orderMapper::toOrderResponse).toList();
        return PageResponse.<OrderResponse>builder()
                .currentPage(page)
                .data(orderResponses)
                .pageSize(pageable.getPageSize())
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
    }

    @Transactional
    public String deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

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
        return "hehe";
    }

    public OrderResponse startOrder(StartOrderReq req) {
        Order order = orderRepository
                .findById(req.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus("pending");
        List<SubOrder> subOrders = order.getSubOrders();
        for (SubOrder subOrder : subOrders) {
            subOrder.setStatus("pending");
            Set<OrderItem> orderItems = subOrder.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                ProductVariant productVariant = orderItem.getProductVariant();
                Product product = productVariant.getProduct();
                Integer purchase = product.getPurchase();
                if (purchase == null) purchase = 0;
                ProductDocument productDocument = productElasticsearchRepository
                        .findById(product.getId() + "")
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                Integer purchaseDoc = productDocument.getPurchase();
                if (purchaseDoc == null) purchaseDoc = 0;
                productDocument.setPurchase(purchaseDoc + orderItem.getQuantity());
                productElasticsearchRepository.save(productDocument);
                product.setPurchase(purchase + orderItem.getQuantity());
                productRepository.save(product);
            }
        }
        UserAddress userAddress = userAddressRepository
                .findById(req.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        order.setUserAddress(userAddress);
        String qrCode = CodeUtil.generateCode(8);
        order.setQRCode(qrCode);
        if (req.getIsQR()) {
            order.setStatus("paying");
            // Generate QR code image and get Cloudinary URL
            try {
                String qrImageUrl = generateOrderQr(order, qrCode);
                // Store the QR image URL in the Order entity - we'll use the same QRCode field
                // but now it will contain the Cloudinary URL instead of just the code
                order.setQRCode(qrImageUrl);
                log.info("QR Code image generated and uploaded: {}", qrImageUrl);
            } catch (Exception e) {
                log.error("Failed to generate QR code image for order {}: {}", order.getId(), e.getMessage());
                // Keep the original QR code if image generation fails
            }
        }
        order.setNote(req.getNote());
        order.setSubOrders(subOrders);

        order = orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

    public OrderResponse getInit() {
        User user = userRepository
                .findByEmail(SecurityUtils.getCurrentUserLogin().get())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Order order = orderRepository
                .findFirstByUserAndStatusOrderByCreatedAtDesc(user, "init")
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    @PreAuthorize("isAuthenticated()")
    public OrderResponse getDetail(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    public SubOrderResponse setStatus(SetStatusOrderReq req) {
        SubOrder subOrder = subOrderRepository
                .findById(req.getSubOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        subOrder.setStatus(req.getStatus());
        return orderMapper.toSubOrderResponse(subOrderRepository.save(subOrder));
    }
    public String generateOrderQr(String orderCode) throws IOException, WriterException {
        Order order=orderRepository.findByQRCode(orderCode)
                .orElseThrow(()->new AppException(ErrorCode.ORDER_NOT_FOUND));
       var fileByte=qrService.generateBankQrFile(order.getTotalAmount()+"",order.getCode(),orderCode);
       return cloudinaryService.uploadFile(fileByte,orderCode,orderCode);
    }
    
    // Overloaded method for generating QR with order and QR code separately
    public String generateOrderQr(Order order, String qrCode) throws IOException, WriterException {
       var fileByte=qrService.generateBankQrFile(order.getTotalAmount()+"",order.getCode(),qrCode);
       return cloudinaryService.uploadFile(fileByte,qrCode,qrCode);
    }
}
