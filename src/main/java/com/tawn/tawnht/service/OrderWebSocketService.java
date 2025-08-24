package com.tawn.tawnht.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.tawn.tawnht.dto.response.NewOrderMessage;
import com.tawn.tawnht.dto.response.SubOrderResponse;
import com.tawn.tawnht.entity.SubOrder;
import com.tawn.tawnht.mapper.OrderMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderWebSocketService {

    SimpMessagingTemplate messagingTemplate;
    OrderMapper orderMapper;

    /**
     * Send new order notification to a specific seller
     * @param subOrder The sub-order that was created
     * @param sellerId The seller ID to notify
     */
    public void sendNewOrderToSeller(SubOrder subOrder, Long sellerId) {
        log.info("Sending new order notification to seller: sellerId={}, subOrderId={}", sellerId, subOrder.getId());

        try {
            // Convert SubOrder to SubOrderResponse for consistent API structure
            SubOrderResponse subOrderResponse = orderMapper.toSubOrderResponse(subOrder);

            // Create the WebSocket message
            NewOrderMessage newOrderMessage = NewOrderMessage.builder()
                    .subOrderId(subOrder.getId())
                    .orderId(subOrder.getOrder().getId())
                    .customerName(subOrder.getOrder().getUser().getFirstName())
                    .customerEmail(subOrder.getOrder().getUser().getEmail())
                    .status(subOrder.getStatus())
                    .subTotal(calculateSubOrderTotal(subOrder))
                    .orderItems(subOrderResponse.getOrderItems())
                    .address(subOrderResponse.getAddress())
                    .createdAt(subOrder.getCreatedAt())
                    .paymentMethod(subOrder.getOrder().getPaymentMethod())
                    .notes(subOrder.getOrder().getNote())
                    .build();

            // Send to seller-specific topic
            String sellerTopic = "/topic/orders/seller/" + sellerId;
            messagingTemplate.convertAndSend(sellerTopic, newOrderMessage);

            // Also send to general new orders topic for dashboard statistics
            messagingTemplate.convertAndSend("/topic/orders/new", newOrderMessage);

            log.info("New order notification sent successfully to seller: {}", sellerId);

        } catch (Exception e) {
            log.error("Failed to send new order notification to seller: {}", sellerId, e);
        }
    }

    /**
     * Send order status update to seller
     * @param subOrder The sub-order with updated status
     * @param sellerId The seller ID to notify
     */
    public void sendOrderStatusUpdate(SubOrder subOrder, Long sellerId) {
        log.info(
                "Sending order status update to seller: sellerId={}, subOrderId={}, status={}",
                sellerId,
                subOrder.getId(),
                subOrder.getStatus());

        try {
            SubOrderResponse subOrderResponse = orderMapper.toSubOrderResponse(subOrder);

            String sellerTopic = "/topic/orders/updates/seller/" + sellerId;
            messagingTemplate.convertAndSend(sellerTopic, subOrderResponse);

            log.info("Order status update sent successfully to seller: {}", sellerId);

        } catch (Exception e) {
            log.error("Failed to send order status update to seller: {}", sellerId, e);
        }
    }

    /**
     * Calculate the total amount for a sub-order
     */
    private java.math.BigDecimal calculateSubOrderTotal(SubOrder subOrder) {
        return subOrder.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
