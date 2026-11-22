package com.cinematography.adminapi.cinematography.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cinematography.adminapi.cinematography.dtos.ApiResponseCode;
import com.cinematography.adminapi.cinematography.dtos.BannerDetails;
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
	private final AdminValidatorBundle adminValidatorBundle;

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
	@PostMapping(value = "/logout", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<ApiResponse<String>> findUserLogoutDetails(HttpServletRequest request,
			@RequestHeader(value = "Authorization") String authorization) {
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

				// Extract the username from token claims
				String username = claims.get(ApiResponseCode.USERNAME, String.class);

				// Fetch the user and update logout date
				Optional<AdminUserValue> userValue = adminServiceBundle.adminService.findByUserDetails(username);
				boolean isFindUserDetailsByName = adminServiceBundle.adminService
						.isUserLogoutDateUpdate(userValue.get());

				// If logout date update is successful, set the response
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

	@Observed
	@Tag(name = "banner", description = "Operations about banner")
	@PostMapping(value = "/banner/add", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<ApiResponse<BannerDetails>> addBannerDetails(@RequestBody BannerDetails bannerDetails) {
		log.info("AdminController addBannerDetails {}", bannerDetails);

		ApiResponse<BannerDetails> apiRes = new ApiResponse<>();
		List<String> messages = new ArrayList<>();
		Errors errors = new BeanPropertyBindingResult(bannerDetails, "bannerDetails");

		bannerDetails.setBannerId(null);

		adminValidatorBundle.bannerDetailsValidator.validate(bannerDetails, errors);

		if (errors.hasErrors()) {
			log.error("AdminController addCustomerDetails validation error {} ", errors.getAllErrors());

			List<String> errorMessages = errors.getAllErrors().stream().map(error -> {
				if (error instanceof FieldError fieldError) {
					return fieldError.getField() + ": " + fieldError.getDefaultMessage();
				} else {
					return error.getObjectName() + ": " + error.getDefaultMessage();
				}
			}).toList();

			messages.addAll(errorMessages);
			apiRes.setMessages(messages);
			apiRes.setStatus(CommonResponseCodes.ERROR);
			apiRes.setStatusCode(CommonResponseCodes.ERROR_CODE);

			return new ResponseEntity<>(ApiResponse.error(apiRes.getMessages(), HttpStatus.BAD_REQUEST),
					HttpStatus.BAD_REQUEST);
		}

		try {
			
			apiRes.setData(bannerDetails);
			apiRes.setStatus(CommonResponseCodes.SUCCESS);

		} catch (Exception e) {
			apiRes.setStatus(CommonResponseCodes.ERROR);
			log.error("AdminController addCustomerDetails error {} ", e.getMessage());

			messages.add(CommonResponseCodes.ERROR_MSG);
			apiRes.setMessages(messages);}

		if (CommonResponseCodes.SUCCESS.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.success(apiRes.getData(), messages, HttpStatus.CREATED), HttpStatus.CREATED);
		} else if (CommonResponseCodes.WARNING.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.warning(apiRes.getData(), messages, HttpStatus.OK), HttpStatus.OK);
		} else if (CommonResponseCodes.ERROR.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.error(apiRes.getMessages(), HttpStatus.INTERNAL_SERVER_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			return new ResponseEntity<>(ApiResponse.empty(), HttpStatusCode.valueOf(200));
		}
	}

	private Jws<Claims> validateAdminToken(String authorizationHeader) {
		// Extract the token by removing the "Bearer " prefix (first 7 characters)
		if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(ApiResponseCode.BEARER)) {
			authorizationHeader = authorizationHeader.substring(7); // Remove "Bearer " prefix
		}

		// Validate and parse the token using jwtUtil
		return infra.getJwtUtil().validateToken(authorizationHeader);
	}
}
