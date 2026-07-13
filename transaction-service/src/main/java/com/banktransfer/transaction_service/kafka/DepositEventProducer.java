package com.banktransfer.transaction_service.kafka;

import com.banktransfer.transaction_service.event.DepositRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositEventProducer {

    private static final String TOPIC = "deposit-requested-topic";

    private final KafkaTemplate<String, DepositRequestedEvent> kafkaTemplate;

    public void publishDepositRequested(DepositRequestedEvent event) {
        log.info("Publication DepositRequested pour la transaction : {}", event.getTransactionId());
        kafkaTemplate.send(TOPIC, event.getTransactionId().toString(), event);
    }
}