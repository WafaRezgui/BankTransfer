package com.banktransfer.auth_service.dto;

import com.banktransfer.auth_service.model.Role;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    private Role role;   // le nouveau rôle souhaité : CLIENT ou ADMIN
}