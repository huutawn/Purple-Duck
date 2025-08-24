package com.tawn.tawnht.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse {
    Long id;
    String userId;
    String userName;
    String content;
    BigDecimal amount;
    String orderCode;
}
