package com.banktransfer.auth_service.dto;

import com.banktransfer.auth_service.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Role role;   // CLIENT ou ADMIN
}