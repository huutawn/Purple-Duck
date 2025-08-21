package com.tawn.tawnht.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAddressResponse {
    Long id;
    String userId;
    String userName;
    String name;
    String phoneNumber;
    String city;
    String district;
    String commune;
    String address;
    Boolean isDefault;
    String addressType;
    LocalDateTime createdAt;
}
