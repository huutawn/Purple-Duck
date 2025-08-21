package com.tawn.tawnht.mapper;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.tawn.tawnht.dto.response.*;
import com.tawn.tawnht.entity.*;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderMapper {
    public OrderResponse toOrderResponse(Order order) {
        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .subOrders(order.getSubOrders().stream()
                        .map(this::toSubOrderResponse)
                        .collect(Collectors.toList()))
                .note(order.getNote())
                .couponCode(order.getCouponCode())
                .status(order.getStatus())
                .QRCode(order.getQRCode())
                .discountAmount(order.getDiscountAmount())
                .createdAt(order.getCreatedAt())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .paymentMethod(order.getPaymentMethod())
                .shippingCarrier(order.getShippingCarrier())
                .shippingFee(order.getShippingFee())
                .taxAmount(order.getTaxAmount())

                .totalAmount(order.getTotalAmount())
                .trackingNumber(order.getTrackingNumber())
                .userAddress(toUserAddressResponse(order.getUserAddress()))
                .userId(order.getUser().getId())
                .userName(order.getUser().getFirstName())
                .build();
        return orderResponse;
    }

    public UserAddressResponse toUserAddressResponse(UserAddress userAddress) {
        UserAddressResponse userAddressResponse = UserAddressResponse.builder()
                .id(userAddress.getId())
                .address(userAddress.getAddress())
                .userId(userAddress.getUser().getId())
                .addressType(userAddress.getAddressType())
                .name(userAddress.getName())
                .phoneNumber(userAddress.getPhoneNumber())
                .city(userAddress.getCity())
                .commune(userAddress.getCommune())
                .createdAt(userAddress.getCreatedAt())
                .district(userAddress.getDistrict())
                .isDefault(userAddress.getIsDefault())
                .build();
        return userAddressResponse;
    }

    public SubOrderResponse toSubOrderResponse(SubOrder subOrder) {
        SubOrderResponse subOrderResponse = SubOrderResponse.builder()
                .orderId(subOrder.getOrder().getId())
                .subOrderId(subOrder.getId())
                .orderItems(subOrder.getOrderItems().stream()
                        .map(this::toOrderItemResponse)
                        .collect(Collectors.toList()))
                .subTotal(subOrder.getSubTotal())
                .address(toUserAddressResponse(subOrder.getOrder().getUserAddress()))
                .status(subOrder.getStatus())
                .createdAt(subOrder.getCreatedAt())
                .build();
        return subOrderResponse;
    }

    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setProductVariant(toProductVariantResponse(orderItem.getProductVariant()));
        response.setQuantity(orderItem.getQuantity());
        response.setPrice(orderItem.getPrice());
        response.setSubTotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return response;
    }

    private ProductVariantResponse toProductVariantResponse(ProductVariant variant) {
        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(variant.getId());
        response.setProductId(variant.getProduct().getId());
        response.setProductName(variant.getProduct().getName());
        response.setSku(variant.getSku());
        response.setPrice(variant.getPrice());
        response.setStock(variant.getStock());
        response.setImage(variant.getImage());
        response.setAttributes(variant.getProductVariantAttributes().stream()
                .map(this::toAttributeResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private AttributeResponse toAttributeResponse(ProductVariantAttribute attr) {
        AttributeResponse response = new AttributeResponse();
        response.setAttributeId(
                attr.getProductAttributeValue().getProductAttribute().getId());
        response.setAttributeName(
                attr.getProductAttributeValue().getProductAttribute().getDisplayName());
        return response;
    }
}
