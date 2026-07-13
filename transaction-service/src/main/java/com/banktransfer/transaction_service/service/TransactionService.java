package com.banktransfer.transaction_service.service;

import com.banktransfer.transaction_service.dto.TransactionRequest;
import com.banktransfer.transaction_service.dto.TransactionResponse;
import com.banktransfer.transaction_service.event.DepositRequestedEvent;
import com.banktransfer.transaction_service.event.TransactionCreatedEvent;
import com.banktransfer.transaction_service.event.TransferRequestedEvent;
import com.banktransfer.transaction_service.kafka.DepositEventProducer;
import com.banktransfer.transaction_service.kafka.TransactionProducer;
import com.banktransfer.transaction_service.kafka.TransferEventProducer;
import com.banktransfer.transaction_service.mapper.TransactionMapper;
import com.banktransfer.transaction_service.model.Transaction;
import com.banktransfer.transaction_service.model.TransactionStatus;
import com.banktransfer.transaction_service.model.TransactionType;
import com.banktransfer.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionProducer transactionProducer;
    private final TransferEventProducer transferEventProducer;
    private final DepositEventProducer depositEventProducer;

    public TransactionResponse createTransaction(TransactionRequest request) {

        Transaction transaction = transactionMapper.toEntity(request);

        boolean needsBalanceCheck = transaction.getFromAccountId() != null;
        boolean isDeposit = transaction.getType() == TransactionType.DEPOSIT;

        // DEPOSIT et les opérations avec vérification de solde repartent
        // maintenant TOUTES en PENDING -- seul un compte inexistant/erreur
        // pourrait faire échouer même un DEPOSIT
        transaction.setStatus((needsBalanceCheck || isDeposit) ? TransactionStatus.PENDING : TransactionStatus.COMPLETED);

        Transaction saved = transactionRepository.save(transaction);

        transactionProducer.publishTransactionCreated(
                TransactionCreatedEvent.builder()
                        .transactionId(saved.getId())
                        .type(saved.getType())
                        .fromAccountId(saved.getFromAccountId())
                        .toAccountId(saved.getToAccountId())
                        .amount(saved.getAmount())
                        .createdAt(saved.getCreatedAt())
                        .build()
        );

        if (needsBalanceCheck) {
            transferEventProducer.publishTransferRequested(
                    TransferRequestedEvent.builder()
                            .transactionId(saved.getId())
                            .fromAccountId(saved.getFromAccountId())
                            .toAccountId(saved.getToAccountId())
                            .amount(saved.getAmount())
                            .build()
            );
        } else if (isDeposit) {
            depositEventProducer.publishDepositRequested(
                    DepositRequestedEvent.builder()
                            .transactionId(saved.getId())
                            .toAccountId(saved.getToAccountId())
                            .amount(saved.getAmount())
                            .build()
            );
        }

        return transactionMapper.toResponse(saved);
    }

    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable avec l'id: " + id));
        return transactionMapper.toResponse(transaction);
    }

    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }
}