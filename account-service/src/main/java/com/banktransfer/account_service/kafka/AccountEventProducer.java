package com.banktransfer.account_service.kafka;

import com.banktransfer.account_service.event.AccountCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventProducer {

    private static final String TOPIC = "account-created-topic";

    private final KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;

    public void publishAccountCreated(AccountCreatedEvent event) {
        log.info("Publication de l'événement AccountCreated pour le compte : {}", event.getIban());
        kafkaTemplate.send(TOPIC, event.getAccountId().toString(), event);
    }
}