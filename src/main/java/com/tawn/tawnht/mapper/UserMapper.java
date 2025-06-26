package com.tawn.tawnht.mapper;

import com.tawn.tawnht.dto.request.UserCreationRequest;
import com.tawn.tawnht.dto.request.UserUpdateRequest;
import com.tawn.tawnht.dto.response.UserResponse;
import com.tawn.tawnht.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
