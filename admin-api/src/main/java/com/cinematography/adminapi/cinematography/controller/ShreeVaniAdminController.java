package com.cinematography.adminapi.cinematography.controller;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cinematography.adminapi.cinematography.dtos.ApiResponseCode;
import com.cinematography.adminapi.cinematography.dtos.BannerDetails;
import com.cinematography.adminapi.cinematography.dtos.ChangePasswordDetails;
import com.cinematography.adminapi.cinematography.dtos.ExpiredPasswordChangeRequest;
import com.cinematography.adminapi.cinematography.dtos.FolderContentDetails;
import com.cinematography.adminapi.cinematography.dtos.FolderDetails;
import com.cinematography.adminapi.cinematography.entities.AdminUserValue;
import com.cinematography.adminapi.exception.PasswordExpiryException;
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

			String decodedUsername = new String(Base64.getDecoder().decode(username), StandardCharsets.UTF_8);
			String decodedPassword = new String(Base64.getDecoder().decode(password), StandardCharsets.UTF_8);
			String token = adminServiceBundle.adminService.findUsersByUP(decodedUsername, decodedPassword, request);

			if (StringUtils.hasText(token)) {
				apiRes.setData(token);
				apiRes.setStatus(CommonResponseCodes.SUCCESS);
			} else {
				apiRes.setStatus(CommonResponseCodes.WARNING);
				messages.add("Invalid credentials or user not found.");
				apiRes.setMessages(messages);
			}

		} catch (PasswordExpiryException ex) {
			messages.add(ex.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(messages, HttpStatus.FORBIDDEN));
		} catch (IllegalArgumentException ex) {
			messages.add("Invalid login request.");
			return ResponseEntity.badRequest().body(ApiResponse.error(messages, HttpStatus.BAD_REQUEST));

		} catch (Exception ex) {

			log.error("AdminController findUserDetails error", ex);
			messages.add("Unable to process login.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(messages, HttpStatus.INTERNAL_SERVER_ERROR));
		}
		if (CommonResponseCodes.SUCCESS.equalsIgnoreCase(
				apiRes.getStatus())) {

			return new ResponseEntity<>(
					ApiResponse.success(
							apiRes.getData(),
							messages,
							HttpStatus.OK),
					HttpStatus.OK);

		} else if (CommonResponseCodes.WARNING.equalsIgnoreCase(
				apiRes.getStatus())) {

			return new ResponseEntity<>(
					ApiResponse.warning(
							apiRes.getData(),
							messages,
							HttpStatus.OK),
					HttpStatus.OK);
		}

		return new ResponseEntity<>(
				ApiResponse.empty(),
				HttpStatus.OK);
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
	@Tag(name = "customer", description = "Operations about customer")
	@PutMapping(value = "/user/changePassword", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<AdminUserValue>> changePassword(
			@RequestBody @NonNull ChangePasswordDetails passwordDetails) {

		log.info("AdminController changePassword {}", passwordDetails);

		ApiResponse<AdminUserValue> apiRes = new ApiResponse<>();
		List<String> messages = new ArrayList<>();
		Errors errors = new BeanPropertyBindingResult(passwordDetails, "changePasswordDetails");

		adminValidatorBundle.changePasswordValidator.validate(passwordDetails, errors);

		if (errors.hasErrors()) {
			log.error("AdminController changePassword validation error {} ", errors.getAllErrors());

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
			AdminUserValue adminUserValue = adminServiceBundle.adminService.getChangePassword(passwordDetails);

			apiRes.setData(adminUserValue);
			apiRes.setStatus(CommonResponseCodes.SUCCESS);
			// Step 3: Send email notification.
			try {

				String email = adminUserValue.getEmail();

				String firstName = adminUserValue.getName() == null
						? ""
						: adminUserValue.getName();

				String surname = adminUserValue.getSurname() == null
						? ""
						: adminUserValue.getSurname();

				String fullName = (firstName + " " + surname).trim();

				if (fullName.isBlank()) {
					fullName = "User";
				}

				String username = adminUserValue.getUsername();

				if (email != null && !email.isBlank()) {

					adminServiceBundle.emailService.sendPasswordChangedEmail(
							email,
							fullName,
							username);

					log.info(
							"Password change email submitted successfully");

				} else {

					log.warn(
							"Password changed but email is missing");
				}

			} catch (Exception mailException) {

				log.error(
						"Password changed but email notification failed",
						mailException);
			}
			apiRes.setMessages(messages);
		} catch (Exception ex) {
			apiRes.setStatus(CommonResponseCodes.ERROR);
			String errorMsg = "AdminController modifyUserDetails error ::::: " + ex.getMessage();
			messages.add(errorMsg);
			log.error(errorMsg);
		}

		if (CommonResponseCodes.SUCCESS.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.success(apiRes.getData(), messages, HttpStatus.CREATED),
					HttpStatus.CREATED);
		} else if (CommonResponseCodes.WARNING.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.warning(apiRes.getData(), messages, HttpStatus.OK), HttpStatus.OK);
		} else if (CommonResponseCodes.ERROR.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.error(apiRes.getMessages(), HttpStatus.INTERNAL_SERVER_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			return new ResponseEntity<>(ApiResponse.empty(), HttpStatusCode.valueOf(200));
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
			apiRes.setMessages(messages);
		}

		if (CommonResponseCodes.SUCCESS.equalsIgnoreCase(apiRes.getStatus())) {
			return new ResponseEntity<>(ApiResponse.success(apiRes.getData(), messages, HttpStatus.CREATED),
					HttpStatus.CREATED);
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

	    @Observed
    @PutMapping(value = "/user/changeExpiredPassword", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> changeExpiredPassword(@RequestBody ExpiredPasswordChangeRequest details) {
        return updateExpiredPassword(details);
    }

    private ResponseEntity<ApiResponse<String>> updateExpiredPassword(ExpiredPasswordChangeRequest details) {
		ApiResponse<AdminUserValue> apiRes = new ApiResponse<>();
        List<String> messages = new ArrayList<>();
		Errors errors = new BeanPropertyBindingResult(details, "expiredPassword");

		adminValidatorBundle.expiredPasswordValidator.validate(details, errors);

		if (errors.hasErrors()) {
			log.error("AdminController changePassword validation error {} ", errors.getAllErrors());

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
            AdminUserValue user = adminServiceBundle.adminService.changeExpiredPassword(details);
            try {
                if (StringUtils.hasText(user.getEmail())) {
                    String name = ((user.getName() == null ? "" : user.getName()) + " " + (user.getSurname() == null ? "" : user.getSurname())).trim();
                    adminServiceBundle.emailService.sendPasswordChangedEmail(user.getEmail(), name.isBlank() ? "User" : name, user.getUsername());
                }
            } catch (Exception ex) {
                log.error("Password changed but notification failed", ex);
            }
            messages.add("Password changed successfully.");
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully.", messages, HttpStatus.OK));
        } catch (IllegalArgumentException ex) {
            messages.add(ex.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(messages, HttpStatus.BAD_REQUEST));
        } catch (Exception ex) {
            log.error("Password change failed", ex);
            messages.add("Unable to change password. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(messages, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

	 @Observed
    @Tag(
        name = "folder-management",
        description = "Operations about folder management"
    )
    @PostMapping(
        value = "/folders",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<FolderDetails>> createFolder(
            @RequestBody FolderDetails folderDetails,
            Principal principal) {

        log.info("FileManagementController createFolder invoked");

        ApiResponse<FolderDetails> apiRes = new ApiResponse<>();

        List<String> messages = new ArrayList<>();

        Errors errors = new BeanPropertyBindingResult(
                folderDetails, "folderDetails");

        folderDetails.setFolderId(null);

        adminValidatorBundle.foldersValidator.validate(
                folderDetails, errors);

        if (errors.hasErrors()) {

            List<String> errorMessages = errors.getAllErrors()
                    .stream()
                    .map(error -> {

                        if (error instanceof FieldError fieldError) {

                            return fieldError.getField()
                                    + ": "
                                    + fieldError.getDefaultMessage();
                        }

                        return error.getObjectName()
                                + ": "
                                + error.getDefaultMessage();

                    }).toList();

            messages.addAll(errorMessages);

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            messages,
                            HttpStatus.BAD_REQUEST));
        }

        try {

            FolderDetails response =
                    adminServiceBundle.fileManagementService
                            .createFolder(
                                    folderDetails,
                                    principal.getName());

            if (response != null) {

                apiRes.setData(response);

                apiRes.setStatus(CommonResponseCodes.SUCCESS);

                messages.add("Folder created successfully.");

            } else {

                apiRes.setStatus(CommonResponseCodes.WARNING);

                messages.add("Unable to create folder.");
            }

        } catch (IllegalArgumentException ex) {

            messages.add(ex.getMessage());

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            messages,
                            HttpStatus.BAD_REQUEST));

        } catch (Exception ex) {

            log.error(
                    "FileManagementController createFolder error",
                    ex);

            messages.add("Unable to process create folder request.");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            messages,
                            HttpStatus.INTERNAL_SERVER_ERROR));
        }

        if (CommonResponseCodes.SUCCESS.equalsIgnoreCase(
                apiRes.getStatus())) {

            return new ResponseEntity<>(
                    ApiResponse.success(
                            apiRes.getData(),
                            messages,
                            HttpStatus.OK),
                    HttpStatus.OK);

        } else if (CommonResponseCodes.WARNING.equalsIgnoreCase(
                apiRes.getStatus())) {

            return new ResponseEntity<>(
                    ApiResponse.warning(
                            apiRes.getData(),
                            messages,
                            HttpStatus.OK),
                    HttpStatus.OK);
        }

        return new ResponseEntity<>(
                ApiResponse.empty(),
                HttpStatus.OK);
    }

    @Observed
    @Tag(
        name = "folder-management",
        description = "Operations about folder management"
    )
    @GetMapping(
        value = "/tree",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<List<FolderDetails>>>
            getAllFolders() {

        log.info("FileManagementController getAllFolders invoked");

        List<String> messages = new ArrayList<>();

        try {

            List<FolderDetails> folders =
                    adminServiceBundle.fileManagementService
                            .getAllFolders();

            return ResponseEntity.ok(
                    ApiResponse.success(
                            folders,
                            messages,
                            HttpStatus.OK));

        } catch (Exception ex) {

            log.error(
                    "FileManagementController getAllFolders error",
                    ex);

            messages.add("Unable to retrieve folder details.");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            messages,
                            HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Observed
    @Tag(
        name = "file-management",
        description = "Operations about file and folder management"
    )
    @GetMapping(
        value = "/contents",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<FolderContentDetails>>
            getFolderContents(
                    @RequestParam(required = false) Long folderId) {

        log.info(
                "FileManagementController getFolderContents {}",
                folderId);

        List<String> messages = new ArrayList<>();

        try {

            FolderContentDetails details =
                    adminServiceBundle.fileManagementService
                            .getFolderContents(folderId);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            details,
                            messages,
                            HttpStatus.OK));

        } catch (IllegalArgumentException ex) {

            messages.add(ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(
                            messages,
                            HttpStatus.NOT_FOUND));

        } catch (Exception ex) {

            log.error(
                    "FileManagementController getFolderContents error",
                    ex);

            messages.add("Unable to retrieve folder contents.");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            messages,
                            HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
