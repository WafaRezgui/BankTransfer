package com.banktransfer.account_service.kafka;

import com.banktransfer.account_service.event.AccountCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventConsumer {

    @KafkaListener(topics = "account-created-topic", groupId = "account-service-group")
    public void consumeAccountCreated(AccountCreatedEvent event) {
        log.info("📩 Événement reçu : nouveau compte créé -> IBAN: {}, solde initial: {}",
                event.getIban(), event.getBalance());
        // Ici, plus tard, ce sera notification-service qui enverra un vrai email/SMS
    }
}