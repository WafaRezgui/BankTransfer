package com.banktransfer.account_service.dto;

import com.banktransfer.account_service.model.AccountType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAccountRequest {
    private Long userId;
    private AccountType type;
    private String cin;
    private String adresse;
    private String telephone;
    private LocalDate dateNaissance;
}