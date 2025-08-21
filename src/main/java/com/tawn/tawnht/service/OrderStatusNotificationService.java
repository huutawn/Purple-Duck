package com.tawn.tawnht.service;

import com.tawn.tawnht.dto.response.OrderStatusMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderStatusNotificationService {

    SimpMessagingTemplate messagingTemplate;

    public void sendOrderStatusUpdate(Long orderId, String qrCode, String status, String message, String userId) {
        log.info("Sending order status update: orderId={}, status={}, userId={}", orderId, status, userId);
        
        OrderStatusMessage statusMessage = OrderStatusMessage.builder()
                .orderId(orderId)
                .qrCode(qrCode)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .build();

        // Send to specific user channel
        messagingTemplate.convertAndSend("/topic/order-status/" + userId, statusMessage);
        
        // Send to general order status channel (optional, for admin monitoring)
        messagingTemplate.convertAndSend("/topic/order-status", statusMessage);
        
        log.info("Order status notification sent successfully");
    }

    public void sendPaymentSuccess(Long orderId, String qrCode, String userId) {
        sendOrderStatusUpdate(
            orderId, 
            qrCode, 
            "paid", 
            "Payment completed successfully. Redirecting to success page...", 
            userId
        );
    }

    public void sendPaymentPending(Long orderId, String qrCode, String userId) {
        sendOrderStatusUpdate(
            orderId, 
            qrCode, 
            "pending", 
            "Payment received. Order is now being processed.", 
            userId
        );
    }
}
