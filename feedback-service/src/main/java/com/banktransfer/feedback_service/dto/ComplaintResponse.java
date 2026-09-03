package com.banktransfer.feedback_service.dto;

import com.banktransfer.feedback_service.model.ComplaintCategory;
import com.banktransfer.feedback_service.model.ComplaintPriority;
import com.banktransfer.feedback_service.model.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {
    private Long id;
    private Long userId;
    private String subject;
    private String description;
    private ComplaintCategory category;
    private ComplaintPriority priority;
    private ComplaintStatus status;
    private Long linkedTransactionId;
    private String adminResponse;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}