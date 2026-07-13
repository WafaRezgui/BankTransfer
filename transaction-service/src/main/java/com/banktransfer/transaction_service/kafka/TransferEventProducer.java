package com.banktransfer.transaction_service.kafka;

import com.banktransfer.transaction_service.event.TransferRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferEventProducer {

    private static final String TOPIC = "transfer-requested-topic";

    private final KafkaTemplate<String, TransferRequestedEvent> kafkaTemplate;

    public void publishTransferRequested(TransferRequestedEvent event) {
        log.info("Publication TransferRequested pour la transaction : {}", event.getTransactionId());
        kafkaTemplate.send(TOPIC, event.getTransactionId().toString(), event);
    }
}