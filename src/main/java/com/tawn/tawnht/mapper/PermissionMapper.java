package com.tawn.tawnht.mapper;

import org.mapstruct.Mapper;

import com.tawn.tawnht.dto.request.PermissionRequest;
import com.tawn.tawnht.dto.response.PermissionResponse;
import com.tawn.tawnht.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
