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
public class DepositRequestedEvent {
    private Long transactionId;
    private Long toAccountId;
    private BigDecimal amount;
}