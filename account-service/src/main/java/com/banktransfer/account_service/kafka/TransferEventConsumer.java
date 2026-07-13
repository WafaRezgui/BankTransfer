package com.banktransfer.account_service.kafka;

import com.banktransfer.account_service.event.BalanceReservationResultEvent;
import com.banktransfer.account_service.event.TransferRequestedEvent;
import com.banktransfer.account_service.model.Account;
import com.banktransfer.account_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferEventConsumer {

    private final AccountRepository accountRepository;
    private final BalanceResultProducer balanceResultProducer;

    @KafkaListener(topics = "transfer-requested-topic", groupId = "account-service-group")
    public void handleTransferRequested(TransferRequestedEvent event) {
        log.info("Reçu TransferRequested : transactionId={}, fromAccount={}, montant={}",
                event.getTransactionId(), event.getFromAccountId(), event.getAmount());

        Optional<Account> sourceOpt = accountRepository.findById(event.getFromAccountId());

        if (sourceOpt.isEmpty()) {
            publishFailure(event, "Compte source introuvable");
            return;
        }

        Account source = sourceOpt.get();

        if (source.getBalance().compareTo(event.getAmount()) < 0) {
            publishFailure(event, "Solde insuffisant");
            return;
        }

        // Débit du compte source
        source.setBalance(source.getBalance().subtract(event.getAmount()));
        accountRepository.save(source);

        log.info("Compte {} débité de {} — nouveau solde : {}",
                source.getId(), event.getAmount(), source.getBalance());

        // Cas WITHDRAWAL : pas de compte destinataire, on s'arrête ici avec succès
        if (event.getToAccountId() == null) {
            publishSuccess(event);
            return;
        }

        // Cas TRANSFER : il faut aussi créditer le compte destinataire
        Optional<Account> destinationOpt = accountRepository.findById(event.getToAccountId());

        if (destinationOpt.isEmpty()) {
            // COMPENSATION : le débit source a déjà eu lieu, il faut le rembourser
            log.warn("Compte destinataire {} introuvable — remboursement du compte source {}",
                    event.getToAccountId(), source.getId());

            source.setBalance(source.getBalance().add(event.getAmount()));
            accountRepository.save(source);

            publishFailure(event, "Compte destinataire introuvable — opération annulée");
            return;
        }

        Account destination = destinationOpt.get();
        destination.setBalance(destination.getBalance().add(event.getAmount()));
        accountRepository.save(destination);

        log.info("Compte {} crédité de {} — nouveau solde : {}",
                destination.getId(), event.getAmount(), destination.getBalance());

        publishSuccess(event);
    }

    private void publishSuccess(TransferRequestedEvent event) {
        balanceResultProducer.publishResult(
                BalanceReservationResultEvent.builder()
                        .transactionId(event.getTransactionId())
                        .success(true)
                        .fromAccountId(event.getFromAccountId())
                        .toAccountId(event.getToAccountId())
                        .amount(event.getAmount())
                        .build()
        );
    }

    private void publishFailure(TransferRequestedEvent event, String reason) {
        log.warn("Échec de la réservation pour la transaction {} : {}", event.getTransactionId(), reason);

        balanceResultProducer.publishResult(
                BalanceReservationResultEvent.builder()
                        .transactionId(event.getTransactionId())
                        .success(false)
                        .reason(reason)
                        .fromAccountId(event.getFromAccountId())
                        .toAccountId(event.getToAccountId())
                        .amount(event.getAmount())
                        .build()
        );
    }
}