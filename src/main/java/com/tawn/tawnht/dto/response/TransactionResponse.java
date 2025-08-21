package com.tawn.tawnht.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

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
