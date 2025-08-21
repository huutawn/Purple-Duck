package com.tawn.tawnht.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    Long id;
    String userId;
    String userName;
    List<CartItemResponse> cartItems;
    String sessionId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
