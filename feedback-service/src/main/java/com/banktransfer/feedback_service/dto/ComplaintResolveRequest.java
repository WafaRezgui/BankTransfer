package com.banktransfer.feedback_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComplaintResolveRequest {
    @NotBlank
    private String adminResponse;
}