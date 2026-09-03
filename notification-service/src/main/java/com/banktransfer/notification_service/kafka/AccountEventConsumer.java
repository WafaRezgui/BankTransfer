package com.banktransfer.notification_service.kafka;

import com.banktransfer.notification_service.client.UserClient;
import com.banktransfer.notification_service.event.AccountCreatedEvent;
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
public class AccountEventConsumer {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;
    private final EmailService emailService;

    @KafkaListener(topics = "account-created-topic", groupId = "notification-service-group")
    public void handleAccountCreated(AccountCreatedEvent event) {

        UserClient.UserInfo user = userClient.getUserById(event.getUserId());

        String message = String.format(
                "Bonjour %s,\n\nVotre nouveau compte bancaire %s a été créé avec succès.\nSolde initial : %s DT.\n\nMerci de votre confiance,\nL'équipe WeBank",
                user.getFirstName(), event.getIban(), event.getBalance()
        );

        Notification notification = Notification.builder()
                .type("ACCOUNT_CREATED")
                .message(message)
                .userId(event.getUserId())
                .build();

        notificationRepository.save(notification);

        emailService.sendEmail(user.getEmail(), "Bienvenue chez WeBank !", message);

        log.info("Notification traitée pour l'utilisateur {} ({})", event.getUserId(), user.getEmail());
    }
}