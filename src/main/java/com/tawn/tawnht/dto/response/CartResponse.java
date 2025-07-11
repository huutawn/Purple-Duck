package com.tawn.tawnht.dto.response;

import com.tawn.tawnht.entity.CartItem;
import com.tawn.tawnht.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
