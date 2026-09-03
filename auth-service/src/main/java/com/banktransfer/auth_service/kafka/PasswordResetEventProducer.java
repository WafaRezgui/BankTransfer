package com.banktransfer.auth_service.kafka;

import com.banktransfer.auth_service.event.PasswordResetRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEventProducer {

    private static final String TOPIC = "password-reset-requested-topic";

    private final KafkaTemplate<String, PasswordResetRequestedEvent> kafkaTemplate;

    public void publishPasswordResetRequested(PasswordResetRequestedEvent event) {
        log.info("Publication de l'événement PasswordResetRequested pour l'utilisateur : {}", event.getUserId());
        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event);
    }
}