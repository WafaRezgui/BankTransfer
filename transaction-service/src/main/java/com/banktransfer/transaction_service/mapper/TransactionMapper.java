package com.banktransfer.transaction_service.mapper;

import com.banktransfer.transaction_service.dto.TransactionRequest;
import com.banktransfer.transaction_service.dto.TransactionResponse;
import com.banktransfer.transaction_service.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Transaction toEntity(TransactionRequest request);

    TransactionResponse toResponse(Transaction transaction);
}