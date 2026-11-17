package com.cinematography.notification;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j 
public class LoginNotificationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd MMM yyyy, hh:mm:ss a z")
            .withZone(ZoneId.of("Asia/Kolkata"));

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public LoginNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress) {

        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Async("loginMailExecutor")
    public void sendLoginSuccessEmail(
            String recipientEmail,
            String username,
            Instant loginTime) {

        if (!StringUtils.hasText(recipientEmail)) {
            log.warn(
                    "Login notification skipped: email missing for user '{}'",
                    username);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromAddress);
            message.setTo(recipientEmail);
            message.setSubject("Shree Vani - Successful Login");

            message.setText(
                    "Hello " + username + ",\n\n"
                            + "You have logged in successfully "
                            + "to the Shree Vani Admin Portal.\n\n"
                            + "Login time: "
                            + FORMATTER.format(loginTime) + "\n\n"
                            + "If this was not you, please change "
                            + "your password and contact support.\n\n"
                            + "Regards,\n"
                            + "Shree Vani Team");

            mailSender.send(message);

        } catch (Exception ex) {
            // Login remains successful if email delivery fails.
            log.error(
                    "Unable to send login notification for user '{}'",
                    username,
                    ex);
        }
    }
}