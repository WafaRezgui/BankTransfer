package com.banktransfer.transaction_service.kafka;

import com.banktransfer.transaction_service.event.BalanceReservationResultEvent;
import com.banktransfer.transaction_service.model.Transaction;
import com.banktransfer.transaction_service.model.TransactionStatus;
import com.banktransfer.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceResultConsumer {

    private final TransactionRepository transactionRepository;

    @KafkaListener(topics = "balance-reservation-result-topic", groupId = "transaction-service-group")
    public void handleBalanceResult(BalanceReservationResultEvent event) {

        Transaction transaction = transactionRepository.findById(event.getTransactionId())
                .orElse(null);

        if (transaction == null) {
            log.warn("Transaction {} introuvable, impossible de mettre à jour le statut", event.getTransactionId());
            return;
        }

        if (event.isSuccess()) {
            transaction.setStatus(TransactionStatus.COMPLETED);
            log.info("Transaction {} marquée COMPLETED", transaction.getId());
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            log.warn("Transaction {} marquée FAILED : {}", transaction.getId(), event.getReason());
        }

        transactionRepository.save(transaction);
    }
}