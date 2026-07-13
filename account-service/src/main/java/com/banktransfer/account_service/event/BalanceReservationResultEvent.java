package com.banktransfer.account_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceReservationResultEvent {
    private Long transactionId;
    private boolean success;
    private String reason;        // null si succès, message d'erreur sinon
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
}