package com.banktransfer.feedback_service.dto;

import com.banktransfer.feedback_service.model.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplaintRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String subject;
    @NotBlank
    private String description;
    @NotNull
    private ComplaintCategory category;
    private Long linkedTransactionId;
}