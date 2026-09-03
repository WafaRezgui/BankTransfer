package com.banktransfer.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintCreatedEvent {
    private Long complaintId;
    private Long userId;
    private String subject;
    private String priority;
}