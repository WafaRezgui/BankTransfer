package com.banktransfer.notification_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;   // ex: "ACCOUNT_CREATED", "TRANSACTION_CREATED"

    @Column(nullable = false, length = 1000)
    private String message;   // le contenu "humain" de la notification

    private Long userId;       // à qui elle est destinée, quand on le sait

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;   // SENT (simulé) ou FAILED

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = NotificationStatus.SENT;
        }
    }
}