package com.banktransfer.feedback_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequest {
    @NotNull
    private Long userId;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    private String comment;
    private Long linkedTransactionId;
}