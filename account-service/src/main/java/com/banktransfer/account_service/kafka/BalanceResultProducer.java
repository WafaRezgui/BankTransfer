package com.banktransfer.account_service.kafka;

import com.banktransfer.account_service.event.BalanceReservationResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceResultProducer {

    private static final String TOPIC = "balance-reservation-result-topic";

    private final KafkaTemplate<String, BalanceReservationResultEvent> kafkaTemplate;

    public void publishResult(BalanceReservationResultEvent event) {
        log.info("Publication BalanceReservationResult : transactionId={}, success={}",
                event.getTransactionId(), event.isSuccess());
        kafkaTemplate.send(TOPIC, event.getTransactionId().toString(), event);
    }
}
