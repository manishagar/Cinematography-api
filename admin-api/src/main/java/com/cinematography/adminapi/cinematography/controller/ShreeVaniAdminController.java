package com.cinematography.adminapi.cinematography.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cinematography.adminapi.cinematography.dtos.ApiResponseCode;
import com.cinematography.adminapi.cinematography.entities.AdminUserValue;
import com.cinematography.core.constants.CommonResponseCodes;
import com.cinematography.core.dto.ApiResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/users/admin")
@RequiredArgsConstructor 
public class ShreeVaniAdminController {

    
	private final AdminServiceBundle adminServiceBundle;
	private final AdminInfraBundle infra;

    @Observed
    @Tag(name = "user-login", description = "Operations about user login")
    @GetMapping("/login")
    public ResponseEntity<ApiResponse<String>> findUserLoginDetails(
            @RequestParam(name = "username", required = true) String username,
            @RequestParam(name = "password", required = true) String password, HttpServletRequest request) {
        log.info("AdminController findUserDetails invoked");

        ApiResponse<String> apiRes = new ApiResponse<>();
        List<String> messages = new ArrayList<>();
        try {
            String decodedUsername = new String(Base64.getDecoder().decode(username));
			String decodedPassword = new String(Base64.getDecoder().decode(password));
            String token = adminServiceBundle.adminService.findUsersByUP(decodedUsername, decodedPassword, request);
					if (StringUtils.hasText(token)) {
				apiRes.setData(token);
				apiRes.setStatus(CommonResponseCodes.SUCCESS);
			} else {
				apiRes.setStatus(CommonResponseCodes.WARNING);
				messages.add("Invalid credentials or user not found.");
				apiRes.setMessages(messages);
			}

		} catch (Exception e) {
			apiRes.setStatus(CommonResponseCodes.ERROR);
			String errorMsg = "AdminController findUserDetails error: " + e.getMessage();
			messages.add(errorMsg);
			log.error(errorMsg);
		}

		if (CommonResponseCodes.SUCCESS.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.success(apiRes.getData(), messages, HttpStatus.OK), HttpStatus.OK);
		} else if (CommonResponseCodes.WARNING.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.warning(apiRes.getData(), messages, HttpStatus.OK), HttpStatus.OK);
		} else if (CommonResponseCodes.ERROR.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.error(apiRes.getMessages(), HttpStatus.NOT_FOUND),
					HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(ApiResponse.empty(), HttpStatus.OK);
		}
	}

	@Observed 
	@Tag(name = "user-logout", description = "Operations about user logout")
	@PostMapping (value = "/logout", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ApiResponse<String>> findUserLogoutDetails(HttpServletRequest request, @RequestHeader (value = "Authorization") String authorization) {
		log.info("AdminController findUserLogoutDetails invoked");

		ApiResponse<String> apiRes = new ApiResponse<>();
		List<String> messages = new ArrayList<>();
		try {
			if (StringUtils.hasText(authorization)) {
				apiRes.setStatus(CommonResponseCodes.WARNING);
				messages.add("Invalid username or user not found.");
				apiRes.setMessages(messages);
				return new ResponseEntity<>(ApiResponse.warning(null, messages, HttpStatus.OK), HttpStatus.OK);
			} else {
			Jws<Claims> claimsJws = validateAdminToken(authorization);
				Claims claims = claimsJws.getPayload();

//	            Extract the username from token claims
				String username = claims.get(ApiResponseCode.USERNAME, String.class);

//	            Fetch the user and update logout date
				Optional<AdminUserValue> userValue = adminServiceBundle.adminService.findByUserDetails(username);
				boolean isFindUserDetailsByName = adminServiceBundle.adminService.isUserLogoutDateUpdate(userValue.get());

//	            If logout date update is successful, set the response
				if (isFindUserDetailsByName) {
					apiRes.setData(authorization);
					apiRes.setStatus(CommonResponseCodes.SUCCESS);
				}

			}

		} catch (Exception e) {
			apiRes.setStatus(CommonResponseCodes.ERROR);
			String errorMsg = "AdminController findUserLogoutDetails error: " + e.getMessage();
			messages.add(errorMsg);
			log.error(errorMsg);
		}

		if (CommonResponseCodes.SUCCESS.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.success(apiRes.getData(), messages, HttpStatus.OK), HttpStatus.OK);
		} else if (CommonResponseCodes.WARNING.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.warning(apiRes.getData(), messages, HttpStatus.OK), HttpStatus.OK);
		} else if (CommonResponseCodes.ERROR.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.error(apiRes.getMessages(), HttpStatus.NOT_FOUND),
					HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(ApiResponse.empty(), HttpStatus.OK);
		}
	}

		private Jws<Claims> validateAdminToken(String authorizationHeader) {
//	    Extract the token by removing the "Bearer " prefix (first 7 characters)
		if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(ApiResponseCode.BEARER)) {
			authorizationHeader = authorizationHeader.substring(7); // Remove "Bearer " prefix
		}

//	    Validate and parse the token using jwtUtil
		return infra.getJwtUtil().validateToken(authorizationHeader);
	}
}
