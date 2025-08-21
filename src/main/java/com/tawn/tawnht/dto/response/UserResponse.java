package com.tawn.tawnht.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String email;
    String firstName;
    String lastName;
    String picture;
    String tokenVerify;
    Boolean isVerified;
    LocalDateTime timeCreateToken;
    LocalDate dob;
    Set<RoleResponse> roles;
}
