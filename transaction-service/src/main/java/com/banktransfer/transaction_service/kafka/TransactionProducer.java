package com.banktransfer.transaction_service.kafka;

import com.banktransfer.transaction_service.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {

    private static final String TOPIC = "transaction-created-topic";

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    public void publishTransactionCreated(TransactionCreatedEvent event) {
        log.info("Publication de l'événement TransactionCreatedEvent pour la transaction id={}", event.getTransactionId());
        kafkaTemplate.send(TOPIC, event.getTransactionId().toString(), event);
    }
}