package com.tawn.tawnht.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
