package com.banktransfer.notification_service.kafka;

import com.banktransfer.notification_service.client.UserClient;
import com.banktransfer.notification_service.event.ComplaintCreatedEvent;
import com.banktransfer.notification_service.event.ComplaintResolvedEvent;
import com.banktransfer.notification_service.mail.EmailService;
import com.banktransfer.notification_service.model.Notification;
import com.banktransfer.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComplaintEventConsumer {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;
    private final EmailService emailService;

    @KafkaListener(topics = "complaint-created-topic", groupId = "notification-service-group")
    public void handleComplaintCreated(ComplaintCreatedEvent event) {
        UserClient.UserInfo user = userClient.getUserById(event.getUserId());

        String message = String.format(
                "Bonjour %s,\n\nNous avons bien reçu votre réclamation : \"%s\".\nPriorité : %s\n\nNotre équipe la traite dans les meilleurs délais.\n\nL'équipe WeBank",
                user.getFirstName(), event.getSubject(), event.getPriority()
        );

        notificationRepository.save(Notification.builder()
                .type("COMPLAINT_CREATED")
                .message(message)
                .userId(event.getUserId())
                .build());

        emailService.sendEmail(user.getEmail(), "Réclamation reçue", message);
    }

    @KafkaListener(topics = "complaint-resolved-topic", groupId = "notification-service-group")
    public void handleComplaintResolved(ComplaintResolvedEvent event) {
        UserClient.UserInfo user = userClient.getUserById(event.getUserId());

        String message = String.format(
                "Bonjour %s,\n\nVotre réclamation \"%s\" a été traitée.\n\nRéponse : %s\n\nL'équipe WeBank",
                user.getFirstName(), event.getSubject(), event.getAdminResponse()
        );

        notificationRepository.save(Notification.builder()
                .type("COMPLAINT_RESOLVED")
                .message(message)
                .userId(event.getUserId())
                .build());

        emailService.sendEmail(user.getEmail(), "Votre réclamation a été traitée", message);
    }
}