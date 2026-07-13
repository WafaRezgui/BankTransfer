package com.banktransfer.auth_service.mapper;


import com.banktransfer.auth_service.dto.AuthResponse;
import com.banktransfer.auth_service.dto.RegisterRequest;
import com.banktransfer.auth_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // On ignore "password" et "id" ici car le password doit être HASHÉ
    // (pas un simple mapping direct) et l'id est généré par la base.
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "userId", source = "id")
    AuthResponse toAuthResponse(User user);
}