package com.tawn.tawnht.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusMessage {
    Long orderId;
    String qrCode;
    String status;
    String message;
    LocalDateTime timestamp;
    String userId;
}
