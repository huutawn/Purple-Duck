package com.tawn.tawnht.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerCreationRequest {
    String storeName;
    String storeDescription;
    String storeLogo;
}
