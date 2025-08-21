package com.tawn.tawnht.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressCreationRequest {
    String city;
    String district;
    String commune;
    String address;
    Boolean isDefault;
    String addressType;
    String phoneNumber;
    String Name;
}
