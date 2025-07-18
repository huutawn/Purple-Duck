package com.tawn.tawnht.mapper;

import com.tawn.tawnht.dto.request.RoleRequest;
import com.tawn.tawnht.dto.response.RoleResponse;
import com.tawn.tawnht.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
