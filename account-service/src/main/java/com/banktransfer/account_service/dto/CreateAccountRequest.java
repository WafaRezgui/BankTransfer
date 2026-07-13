package com.banktransfer.account_service.dto;

import com.banktransfer.account_service.model.AccountType;
import lombok.Data;

@Data
public class CreateAccountRequest {
    private Long userId;
    private AccountType type;
}