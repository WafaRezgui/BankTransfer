package com.banktransfer.account_service.kafka;

import com.banktransfer.account_service.event.DepositRequestedEvent;
import com.banktransfer.account_service.event.DepositResultEvent;
import com.banktransfer.account_service.model.Account;
import com.banktransfer.account_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositEventConsumer {

    private final AccountRepository accountRepository;
    private final DepositResultProducer depositResultProducer;

    @KafkaListener(topics = "deposit-requested-topic", groupId = "account-service-group")
    public void handleDepositRequested(DepositRequestedEvent event) {

        Account account = accountRepository.findById(event.getToAccountId()).orElse(null);

        if (account == null) {
            log.warn("Compte {} introuvable, dépôt {} rejeté", event.getToAccountId(), event.getTransactionId());

            depositResultProducer.publishResult(
                    DepositResultEvent.builder()
                            .transactionId(event.getTransactionId())
                            .success(false)
                            .reason("Compte destinataire introuvable")
                            .toAccountId(event.getToAccountId())
                            .amount(event.getAmount())
                            .build()
            );
            return;
        }

        account.setBalance(account.getBalance().add(event.getAmount()));
        accountRepository.save(account);

        log.info("Compte {} crédité de {} — nouveau solde : {}",
                account.getId(), event.getAmount(), account.getBalance());

        depositResultProducer.publishResult(
                DepositResultEvent.builder()
                        .transactionId(event.getTransactionId())
                        .success(true)
                        .toAccountId(event.getToAccountId())
                        .amount(event.getAmount())
                        .build()
        );
    }
}