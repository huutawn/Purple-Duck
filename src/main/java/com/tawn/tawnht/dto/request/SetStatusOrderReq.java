package com.tawn.tawnht.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetStatusOrderReq {
    @NotNull
    Long subOrderId;

    @NotNull
    String status;
}
