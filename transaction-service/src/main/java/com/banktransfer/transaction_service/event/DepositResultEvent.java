package com.banktransfer.transaction_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositResultEvent {
    private Long transactionId;
    private boolean success;
    private String reason;
    private Long toAccountId;
    private BigDecimal amount;
}