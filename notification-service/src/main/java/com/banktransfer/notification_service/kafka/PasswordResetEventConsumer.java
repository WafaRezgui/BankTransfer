package com.banktransfer.notification_service.kafka;

import com.banktransfer.notification_service.client.UserClient;
import com.banktransfer.notification_service.event.PasswordResetRequestedEvent;
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
public class PasswordResetEventConsumer {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;
    private final EmailService emailService;

    @KafkaListener(topics = "password-reset-requested-topic", groupId = "notification-service-group")
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {

        UserClient.UserInfo user = userClient.getUserById(event.getUserId());

        // Lien pointant vers le frontend Angular (page de reset à créer plus tard)
        String resetLink = "http://localhost:4200/reset-password?token=" + event.getToken();

        String message = String.format(
                "Bonjour %s,\n\nVous avez demandé la réinitialisation de votre mot de passe.\n" +
                        "Cliquez sur le lien suivant pour choisir un nouveau mot de passe (valable 15 minutes) :\n%s\n\n" +
                        "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                        "Merci,\nL'équipe WeBank",
                user.getFirstName(), resetLink
        );

        Notification notification = Notification.builder()
                .type("PASSWORD_RESET_REQUESTED")
                .message(message)
                .userId(event.getUserId())
                .build();

        notificationRepository.save(notification);

        emailService.sendEmail(user.getEmail(), "Réinitialisation de votre mot de passe WeBank", message);

        log.info("Notification de reset mot de passe envoyée pour l'utilisateur {} ({})", event.getUserId(), user.getEmail());
    }
}