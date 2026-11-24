package com.cinematography.adminapi.cinematography.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordExpiryEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public void sendPasswordExpiryEmail(
            String email,
            String username,
            LocalDateTime expiryDate,
            long remainingDays) {

        String formattedExpiryDate = expiryDate
                .atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                .format(FORMATTER) + " IST";

        String subject;
        String message;

        if (remainingDays <= 0) {

            subject = "Password Expired - Action Required";

            message = """
                    Dear %s,

                    Your Shree Vani Jagat account password
                    has expired.

                    Password expiration date: %s

                    Please change your password to regain
                    access to your account.

                    Regards,
                    Shree Vani Jagat Support Team
                    """.formatted(
                    username,
                    formattedExpiryDate);

        } else {

            subject = "Password Expiry Reminder - "
                    + remainingDays + " Days Remaining";

            message = """
                    Dear %s,

                    This is a reminder that your Shree Vani
                    Jagat account password will expire in
                    %d day(s).

                    Password expiration date: %s

                    Please change your password before
                    the expiration date to avoid any
                    interruption in accessing your account.

                    Regards,
                    Shree Vani Jagat Support Team
                    """.formatted(
                    username,
                    remainingDays,
                    formattedExpiryDate);
        }

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom(fromEmail);
        mail.setTo(email);
        mail.setSubject(subject);
        mail.setText(message);

        mailSender.send(mail);

        log.info(
                "Password expiry notification sent for user '{}'",
                username);
    }
}