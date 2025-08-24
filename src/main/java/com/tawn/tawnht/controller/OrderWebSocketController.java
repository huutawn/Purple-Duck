package com.tawn.tawnht.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.tawn.tawnht.entity.User;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.repository.jpa.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderWebSocketController {

    SimpMessagingTemplate messagingTemplate;
    UserRepository userRepository;

    /**
     * Handle seller subscription to their order notifications
     * This endpoint can be used by sellers to confirm they are ready to receive notifications
     */
    @MessageMapping("/orders/seller/subscribe")
    public void subscribeToSellerOrders(@Payload String message, Principal principal) {
        log.info("Seller subscribing to order notifications: {}", principal.getName());

        try {
            String email = principal.getName();
            User user =
                    userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            if (user.getSeller() == null) {
                log.warn("User {} attempted to subscribe to seller orders but is not a seller", email);
                return;
            }

            Long sellerId = user.getSeller().getId();

            // Send confirmation message
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/orders/subscription-confirmed",
                    "Successfully subscribed to order notifications for seller: " + sellerId);

            log.info("Seller {} successfully subscribed to order notifications", sellerId);

        } catch (Exception e) {
            log.error("Error subscribing seller to order notifications", e);
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    "Failed to subscribe to order notifications: " + e.getMessage());
        }
    }

    /**
     * Handle order acknowledgment from seller
     * This can be used when a seller acknowledges they have seen a new order
     */
    @MessageMapping("/orders/acknowledge")
    public void acknowledgeOrder(@Payload Long subOrderId, Principal principal) {
        log.info("Seller acknowledging order: subOrderId={}, seller={}", subOrderId, principal.getName());

        try {
            String email = principal.getName();
            User user =
                    userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            if (user.getSeller() == null) {
                log.warn("User {} attempted to acknowledge order but is not a seller", email);
                return;
            }

            Long sellerId = user.getSeller().getId();

            // You can add logic here to mark the order as acknowledged in the database
            // For now, just log the acknowledgment
            log.info("Order {} acknowledged by seller {}", subOrderId, sellerId);

            // Send confirmation
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/orders/acknowledge-confirmed",
                    "Order " + subOrderId + " acknowledged successfully");

        } catch (Exception e) {
            log.error("Error acknowledging order: subOrderId={}", subOrderId, e);
            messagingTemplate.convertAndSendToUser(
                    principal.getName(), "/queue/errors", "Failed to acknowledge order: " + e.getMessage());
        }
    }

    /**
     * Send a test message to verify WebSocket connection
     */
    @MessageMapping("/orders/test")
    public void testConnection(@Payload String message, Principal principal) {
        log.info("Test message received from {}: {}", principal.getName(), message);

        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/test", "Test message received: " + message);
    }
}
