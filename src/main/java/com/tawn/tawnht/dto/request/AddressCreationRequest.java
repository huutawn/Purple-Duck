package com.tawn.tawnht.dto.request;

import com.tawn.tawnht.entity.Order;
import com.tawn.tawnht.entity.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
