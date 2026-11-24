package com.cinematography.adminapi.cinematography.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import com.cinematography.adminapi.cinematography.entities.AdminUserValue;
import com.cinematography.adminapi.cinematography.repository.AdminUserRepository;

// Import AdminUserRepository from your existing package

@Service
@RequiredArgsConstructor
public class PasswordExpiryNotificationService {

    private final AdminUserRepository adminUserRepository;

    private final PasswordExpiryEmailService
            passwordExpiryEmailService;

    @Value("${app.password.first-reminder-days:15}")
    private int firstReminderDays;

    @Value("${app.password.second-reminder-days:7}")
    private int secondReminderDays;

    @Transactional
    public void processPasswordExpiry(Long userId) {

        AdminUserValue user = adminUserRepository
                .findByUserIdForUpdate(userId)
                .orElse(null);

        if (user == null
                || !"ACTIVE".equals(user.getStatus())
                || user.getExpiry() == null
                || !StringUtils.hasText(user.getEmail())) {

            return;
        }

        LocalDateTime currentDateTime =
                LocalDateTime.now(ZoneOffset.UTC);

        LocalDateTime expiryDate = user.getExpiry();

        // Case 1: Password expired
        if (!expiryDate.isAfter(currentDateTime)) {

            if (!user.isExpiryEmailSent()) {

                passwordExpiryEmailService
                        .sendPasswordExpiryEmail(
                                user.getEmail(),
                                user.getUsername(),
                                expiryDate,
                                0);

                user.setExpiryEmailSent(true);
                user.setExpiry15DaysEmailSent(true);
                user.setExpiry7DaysEmailSent(true);

                adminUserRepository.save(user);
            }

            return;
        }

        long remainingSeconds = Duration.between(
                currentDateTime,
                expiryDate).getSeconds();

        long remainingDays = Math.max(
                1,
                (long) Math.ceil(
                        remainingSeconds / 86400.0));

        // Case 2: Seven days or fewer remaining
        if (!expiryDate.isAfter(
                currentDateTime.plusDays(secondReminderDays))) {

            if (!user.isExpiry7DaysEmailSent()) {

                passwordExpiryEmailService
                        .sendPasswordExpiryEmail(
                                user.getEmail(),
                                user.getUsername(),
                                expiryDate,
                                remainingDays);

                user.setExpiry7DaysEmailSent(true);
                user.setExpiry15DaysEmailSent(true);

                adminUserRepository.save(user);
            }

            return;
        }

        // Case 3: Fifteen days or fewer remaining
        if (!expiryDate.isAfter(
                currentDateTime.plusDays(firstReminderDays))) {

            if (!user.isExpiry15DaysEmailSent()) {

                passwordExpiryEmailService
                        .sendPasswordExpiryEmail(
                                user.getEmail(),
                                user.getUsername(),
                                expiryDate,
                                remainingDays);

                user.setExpiry15DaysEmailSent(true);

                adminUserRepository.save(user);
            }
        }
    }
}