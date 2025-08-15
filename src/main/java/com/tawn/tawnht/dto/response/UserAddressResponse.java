package com.tawn.tawnht.dto.response;

import com.tawn.tawnht.entity.User;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

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
