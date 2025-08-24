package com.tawn.tawnht.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.tawn.tawnht.dto.request.SePayWebhookRequest;
import com.tawn.tawnht.dto.response.TransactionResponse;
import com.tawn.tawnht.entity.Order;
import com.tawn.tawnht.entity.Transaction;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.repository.jpa.OrderRepository;
import com.tawn.tawnht.repository.jpa.TransactionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SePayWebHookService {

    OrderRepository orderRepository;
    TransactionRepository transactionRepository;
    OrderStatusNotificationService orderStatusNotificationService;

    public TransactionResponse getTransaction(SePayWebhookRequest request) {
        String webhookContent = request.getContent();
        log.info("Webhook content received: {}", webhookContent);

        String qrcode = "";

        Pattern pattern = Pattern.compile("\\b([a-zA-Z0-9]{8})\\b");
        Matcher matcher = pattern.matcher(webhookContent);

        if (matcher.find()) {
            qrcode = matcher.group(1);
        } else {
            log.error("Không tìm thấy mã 8 ký tự hợp lệ trong nội dung: '{}'", webhookContent);
            throw new IllegalArgumentException("Nội dung webhook không hợp lệ.");
        }

        log.info("Processing order with QR code extracted by regex: {}", qrcode);
        Order order =
                orderRepository.findByQRCode(qrcode).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String previousStatus = order.getStatus();
        order.setStatus("pending");
        orderRepository.save(order);

        // Send WebSocket notification for payment success
        orderStatusNotificationService.sendPaymentPending(
                order.getId(), order.getQRCode(), order.getUser().getId().toString());

        log.info(
                "Payment webhook processed: Order {} status changed from {} to pending", order.getId(), previousStatus);

        Transaction transaction = Transaction.builder()
                .userName(order.getUser().getFirstName())
                .amount(request.getTransferAmount())
                .userId(order.getUser().getId())
                .orderCode(order.getQRCode())
                .build();
        transaction = transactionRepository.save(transaction);
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .content(transaction.getContent())
                .orderCode(transaction.getOrderCode())
                .userId(transaction.getUserId())
                .userName(transaction.getUserName())
                .build();
    }
}
