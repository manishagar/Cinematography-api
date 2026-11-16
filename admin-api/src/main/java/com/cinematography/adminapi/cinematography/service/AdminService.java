package com.cinematography.adminapi.cinematography.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cinematography.adminapi.cinematography.dtos.ApiResponseCode;
import com.cinematography.adminapi.cinematography.entities.AdminUserValue;
import com.cinematography.adminapi.cinematography.repository.AdminUserRepository;
import com.cinematography.cust.service.EncryptionService;
import com.cinematography.cust.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
	private final AdminUserRepository adminUserRepository;
	@Value("${microservice.security.salt}")
	private String salt;
	private final JwtService jwtService;

	public String findUsersByUP(String username, String password, HttpServletRequest request) {
		String encryptPassword = EncryptionService.encrypt(password, salt);

		String token = null;
		Optional<AdminUserValue> findUsersByUP = Optional.empty();

		try {
			// Normalize issuer/server info
			String serverName = request.getServerName();
			if (StringUtils.hasText(serverName)) {
				serverName = serverName.toLowerCase();
			}
			log.info("UsersService ServerName ::::::::::: {} ", serverName);
			String issuerName = request.getHeader("X-Issuer-Name");
			log.info("UsersService IssuerName ::::::::::: {} ", issuerName);

			// Base claims
			Map<String, Object> claims = new HashMap<>();
			claims.put("typ", "JWT");
			claims.put("iss", issuerName);
			claims.put("username", username);

			// Fetch user by username
			findUsersByUP = adminUserRepository.findUsersByUserName(username);

			if (findUsersByUP.isPresent()) {
				AdminUserValue user = findUsersByUP.get();
				String stored = user.getPassword();

				boolean matchEncrypted = encryptPassword.equals(stored);

				if (matchEncrypted) {
					// Common claims for successful auth
					claims.put(ApiResponseCode.EMAILID, user.getEmail());

					if (user.getStatus().equals(ApiResponseCode.ACTIVE)) {

						// Update login timestamp only on success
						user.setLoginDt(LocalDateTime.now(ZoneId.systemDefault()));
						adminUserRepository.save(user);

						// Generate JWT
						token = jwtService.generateToken(username, claims);
					}
				} else {
					// Password mismatch
					log.warn("Authentication failed for user '{}': password mismatch", username);
					// token remains null; you might throw a custom exception instead
				}
			} else {
				log.warn("No user found for username '{}'", username);
			}
		} catch (Exception e) {
			log.error("UserService findUsersByUP, Unable to generate token ::::", e.getMessage());
		}

		return token;
	}


 @Transactional 
	public Optional<AdminUserValue> findByUserDetails(String username) {
//		Initialize the optional result
		Optional<AdminUserValue> byUserDetails = Optional.empty();

		try {
//			Fetch user details from the repository by username
			byUserDetails = adminUserRepository.findUsersByUserName(username);
		} catch (Exception e) {
//			Log any errors during the query
			log.error("UsersService findByUserDetails :: {}", e.getMessage());
		}

		return byUserDetails;
	}


	public boolean isUserLogoutDateUpdate(AdminUserValue user) {
		boolean isFlag = false;

//		Proceed only if the user is not null
		if (user != null) {
//			Set the logout date to the current timestamp
			user.setLogoutDt(LocalDateTime.now(ZoneId.systemDefault()));

//			Save the updated user entity to the database
			adminUserRepository.save(user);

//			Set success flag to true
			isFlag = true;
		}

		return isFlag;
	}

}
