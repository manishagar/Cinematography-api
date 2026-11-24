package com.cinematography.adminapi.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cinematography.adminapi.cinematography.entities.AdminUserValue;
import com.cinematography.adminapi.cinematography.repository.AdminUserRepository;
import com.cinematography.adminapi.cinematography.service.PasswordExpiryNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PasswordExpiryScheduler {

    private final AdminUserRepository adminUserRepository;

    private final PasswordExpiryNotificationService
            passwordExpiryNotificationService;

    @Value("${app.password.first-reminder-days:15}")
    private int firstReminderDays;

    @Scheduled(
        cron = "${app.password.scheduler-cron}",
        zone = "${app.password.scheduler-zone}"
    )
    public void checkPasswordExpiry() {

        log.info("Password expiry scheduler started");

        LocalDateTime currentDateTime =
                LocalDateTime.now(ZoneOffset.UTC);

        LocalDateTime reminderDate =
                currentDateTime.plusDays(firstReminderDays);

        log.info("Current date time: {}", currentDateTime);
        log.info("Reminder date: {}", reminderDate);

        List<AdminUserValue> users =
                adminUserRepository
                        .findUsersForPasswordExpiry(reminderDate);

        log.info("Total eligible users: {}", users.size());

        for (AdminUserValue user : users) {

            try {

                log.info(
                        "Processing password expiry for user: {}",
                        user.getUsername());

                passwordExpiryNotificationService
                        .processPasswordExpiry(user.getUserId());

            } catch (Exception ex) {

                log.error(
                        "Password expiry email failed for user: {}",
                        user.getUsername(),
                        ex);
            }
        }

        log.info("Password expiry scheduler completed");
    }
}