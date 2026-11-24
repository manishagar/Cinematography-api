package com.cinematography.adminapi.cinematography.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value ("${app.mail.from}") String fromEmail) {

        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendPasswordChangedEmail(
            String email,
            String name,
            String username) {

        String subject = "Shree Vani - Password Changed Successfully";

        String message = """
                Dear %s,

                Your Shree Vani account password
                has been changed successfully.

                Username: %s
                Password: %s

                If you made this change, no further
                action is required.

                If you did not change your password,
                please contact the administrator immediately.

                Regards,
                Shree Vani Team
                """.formatted(name, username);

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom(fromEmail);
        mail.setTo(email);
        mail.setSubject(subject);
        mail.setText(message);

        mailSender.send(mail);
    }
}
