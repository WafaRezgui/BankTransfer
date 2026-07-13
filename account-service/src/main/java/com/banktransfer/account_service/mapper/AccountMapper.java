package com.banktransfer.account_service.mapper;

import com.banktransfer.account_service.dto.AccountResponse;
import com.banktransfer.account_service.dto.CreateAccountRequest;
import com.banktransfer.account_service.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    // iban, balance, status, id, createdAt : tous générés/calculés,
    // jamais fournis par le client -> on les ignore ici
    @Mapping(target = "iban", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Account toEntity(CreateAccountRequest request);

    AccountResponse toResponse(Account account);   // ici tous les champs correspondent -> mapping 100% automatique
}
