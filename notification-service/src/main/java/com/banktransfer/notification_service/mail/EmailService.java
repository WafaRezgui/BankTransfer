package com.banktransfer.notification_service.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("WeBank2710@gmail.com");

            mailSender.send(message);

            log.info("✅ Email envoyé avec succès à {}", to);
        } catch (Exception e) {
            log.error("❌ Échec de l'envoi d'email à {} : {}", to, e.getMessage());
        }
    }
}