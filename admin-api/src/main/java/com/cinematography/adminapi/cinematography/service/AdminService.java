package com.cinematography.adminapi.cinematography.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cinematography.adminapi.cinematography.dtos.ApiResponseCode;
import com.cinematography.adminapi.cinematography.dtos.ChangePasswordDetails;
import com.cinematography.adminapi.cinematography.dtos.ExpiredPasswordChangeRequest;
import com.cinematography.adminapi.cinematography.entities.AdminUserValue;
import com.cinematography.adminapi.cinematography.repository.AdminUserRepository;
import com.cinematography.adminapi.exception.PasswordExpiryException;
import com.cinematography.cust.service.EncryptionService;
import com.cinematography.cust.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;

import com.cinematography.notification.LoginNotificationService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
	private final AdminUserRepository adminUserRepository;
	@Value("${microservice.security.salt}")
	private String salt;
	@Value("${app.password.expiration-days:90}")
	private int passwordExpirationDays;
	private final JwtService jwtService;
	private final LoginNotificationService loginNotificationService;

	public String findUsersByUP(
			String username,
			String password,
			HttpServletRequest request) {

		String encryptPassword = EncryptionService.encrypt(password, salt);

		String token = null;

		try {

			String serverName = request.getHeader("X-Issuer-Name");

			if (!StringUtils.hasText(serverName)) {
				serverName = request.getServerName();
			}

			if (StringUtils.hasText(serverName)) {
				serverName = serverName.toLowerCase();
			}

			log.info("UsersService ServerName ::::::::::: {}",
					serverName);

			Map<String, Object> claims = new HashMap<>();

			claims.put("typ", "JWT");
			claims.put("iss", serverName);
			claims.put("username", username);

			Optional<AdminUserValue> findUsersByUP = adminUserRepository.findUsersByUserName(username);

			if (findUsersByUP.isEmpty()) {

				log.warn("No active user found for username '{}'",
						username);

				return null;
			}

			AdminUserValue user = findUsersByUP.get();

			String stored = user.getPassword();

			boolean matchEncrypted = encryptPassword.equals(stored);

			if (!matchEncrypted) {

				log.warn("Authentication failed for user '{}'",
						username);

				return null;
			}

			if (!ApiResponseCode.ACTIVE.equals(user.getStatus())) {
				return null;
			}

			// Password expiration validation
			// Password expiration validation
			LocalDateTime currentDateTime = LocalDateTime.now(ZoneOffset.UTC);

			LocalDateTime passwordExpiryDate = user.getExpiry();

			log.info("Password expiry validation started");

			log.info("Username: {}", user.getUsername());

			log.info("Current UTC date time: {}", currentDateTime);

			log.info("Password expiry date: {}", passwordExpiryDate);

			boolean passwordExpired = passwordExpiryDate == null
					|| !passwordExpiryDate.isAfter(currentDateTime);

			log.info("Is password expired: {}", passwordExpired);

			if (passwordExpired) {

				log.warn(
						"Password expired for user: {}",
						user.getUsername());

				throw new PasswordExpiryException(
						"Your password has expired. "
								+ "Please change your password.");
			}

			log.info("Password expiry validation successful");

			// Common claims for successful authentication
			claims.put(ApiResponseCode.EMAILID, user.getEmail());

			// Update successful login timestamp
			user.setLoginDt(
					LocalDateTime.now(ZoneId.systemDefault()));

			adminUserRepository.save(user);

			try {

				loginNotificationService.sendLoginSuccessEmail(
						user.getEmail(),
						user.getUsername(),
						user.getLoginDt()
								.atZone(ZoneId.systemDefault())
								.toInstant());

			} catch (TaskRejectedException ex) {

				log.warn(
						"Login succeeded but email could not "
								+ "be queued for '{}'",
						username,
						ex);
			}

			// Generate JWT only if password is valid
			token = jwtService.generateToken(username, claims);

		} catch (PasswordExpiryException ex) {

			// Do not suppress the password expiration exception
			throw ex;

		} catch (Exception ex) {

			log.error(
					"UserService findUsersByUP: "
							+ "Unable to generate token",
					ex);

			throw new IllegalStateException(
					"Unable to process login.", ex);
		}

		return token;
	}

	@Transactional
	public Optional<AdminUserValue> findByUserDetails(String username) {
		// Initialize the optional result
		Optional<AdminUserValue> byUserDetails = Optional.empty();

		try {
			// Fetch user details from the repository by username
			byUserDetails = adminUserRepository.findUsersByUserName(username);
		} catch (Exception e) {
			// Log any errors during the query
			log.error("UsersService findByUserDetails :: {}", e.getMessage());
		}

		return byUserDetails;
	}

	public boolean isUserLogoutDateUpdate(AdminUserValue user) {
		boolean isFlag = false;

		// Proceed only if the user is not null
		if (user != null) {
			// Set the logout date to the current timestamp
			user.setLogoutDt(LocalDateTime.now(ZoneId.systemDefault()));

			// Save the updated user entity to the database
			adminUserRepository.save(user);

			// Set success flag to true
			isFlag = true;
		}

		return isFlag;
	}

	public AdminUserValue getChangePassword(ChangePasswordDetails changePasswordDetails) {

		// Retrieve the existing user based on the supplied username. If no matching
		// user is found, throw an IllegalArgumentException.
		AdminUserValue user = adminUserRepository.findUsersByUserName(changePasswordDetails.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("username: not found"));

		LocalDateTime currentDateTime = LocalDateTime.now(ZoneOffset.UTC);

		// Keep the password hash used by existing consumers.
		byte[] securePassword = EncryptionService.hash(changePasswordDetails.getPassword().toCharArray(),
				salt.getBytes());
		String hashCode = Base64.getEncoder().encodeToString(securePassword);
		user.setHash(hashCode);

		user.setPassword(
				EncryptionService.encrypt(
						changePasswordDetails.getPassword(),
						salt));
		user.setSalt(salt);
		user.setPasswordChangedDt(currentDateTime);
		user.setExpiry(currentDateTime.plusDays(passwordExpirationDays));
		user.setExpiry15DaysEmailSent(false);
		user.setExpiry7DaysEmailSent(false);
		user.setExpiryEmailSent(false);
		user.setUpdatedDt(currentDateTime);
		user.setModifiedBy(user.getUsername());

		adminUserRepository.save(user);

		return user;
	}

	    @Transactional
    public AdminUserValue changeExpiredPassword(ExpiredPasswordChangeRequest details) {
        return updateExpiredPassword(details);
    }

    private AdminUserValue updateExpiredPassword(ExpiredPasswordChangeRequest details) {
        if (details == null || !StringUtils.hasText(details.getUsername()) || !StringUtils.hasText(details.getConfirmPassword())) {
            throw new IllegalArgumentException("currentPassword: Invalid credentials.");
        }
        AdminUserValue user = adminUserRepository.findByAdminIdForUpdate(details.getUsername())
            .filter(u -> ApiResponseCode.ACTIVE.equals(u.getStatus()))
            .orElseThrow(() -> new IllegalArgumentException("currentPassword: Invalid credentials."));
        if (!EncryptionService.encrypt(details.getCurrentPassword(), salt).equals(user.getPassword())) {
            throw new IllegalArgumentException("currentPassword: Invalid credentials.");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (user.getExpiry() == null || user.getExpiry().isAfter(now)) {
            throw new IllegalArgumentException("Password is not expired. Please sign in.");
        }
      byte[] securePassword = EncryptionService.hash(details.getPassword().toCharArray(),
				salt.getBytes());
		String hashCode = Base64.getEncoder().encodeToString(securePassword);
		user.setHash(hashCode);


		user.setSalt(salt);
        user.setPasswordChangedDt(now);
        user.setExpiry(now.plusDays(passwordExpirationDays));
        user.setExpiry15DaysEmailSent(false);
        user.setExpiry7DaysEmailSent(false);
        user.setExpiryEmailSent(false);
        user.setUpdatedDt(now);
        user.setModifiedBy(user.getUsername());
        return adminUserRepository.save(user);
    }

}
