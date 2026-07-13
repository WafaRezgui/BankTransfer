package com.banktransfer.account_service.service;

import com.banktransfer.account_service.dto.AccountResponse;
import com.banktransfer.account_service.dto.CreateAccountRequest;
import com.banktransfer.account_service.event.AccountCreatedEvent;
import com.banktransfer.account_service.kafka.AccountEventProducer;
import com.banktransfer.account_service.mapper.AccountMapper;
import com.banktransfer.account_service.model.Account;
import com.banktransfer.account_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountEventProducer accountEventProducer;   // <-- ajouté

    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = accountMapper.toEntity(request);
        account.setIban(generateIban());

        Account saved = accountRepository.save(account);

        // Publie l'événement APRÈS la sauvegarde réussie
        accountEventProducer.publishAccountCreated(
                AccountCreatedEvent.builder()
                        .accountId(saved.getId())
                        .userId(saved.getUserId())
                        .iban(saved.getIban())
                        .balance(saved.getBalance())
                        .build()
        );

        return accountMapper.toResponse(saved);
    }

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable : " + id));
        return accountMapper.toResponse(account);
    }

    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    private String generateIban() {
        return "TN59" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}