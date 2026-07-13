package com.banktransfer.account_service.kafka;

import com.banktransfer.account_service.event.DepositResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositResultProducer {

    private static final String TOPIC = "deposit-result-topic";

    private final KafkaTemplate<String, DepositResultEvent> kafkaTemplate;

    public void publishResult(DepositResultEvent event) {
        log.info("Publication DepositResult : transactionId={}, success={}",
                event.getTransactionId(), event.isSuccess());
        kafkaTemplate.send(TOPIC, event.getTransactionId().toString(), event);
    }
}