package com.tawn.tawnht.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    String title;
    String message;
    String type; // ORDER, PAYMENT, SYSTEM, PROMOTION
    Boolean isRead;
    String relatedEntityId; // ID of related order, product, etc.
    String relatedEntityType; // ORDER, PRODUCT, USER
    LocalDateTime createdAt;
}
