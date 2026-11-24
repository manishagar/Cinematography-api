package com.cinematography.adminapi.cinematography.validator;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.cinematography.adminapi.cinematography.dtos.ApiResponseCode;
import com.cinematography.adminapi.cinematography.dtos.ChangePasswordDetails;
import com.cinematography.adminapi.cinematography.repository.AdminUserRepository;
import com.cinematography.cust.service.EncryptionService;
import com.cinematography.cust.validation.PasswordValidator;
import com.cinematography.adminapi.cinematography.entities.AdminUserValue;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data 
@Slf4j 
@Component 
@RequiredArgsConstructor 
public class ChangePasswordValidator implements Validator {

    private final PasswordValidator passwordValidator;
    private final AdminUserRepository adminUserRepository;

    @Value("${microservice.security.salt}")
	private String salt;

    @Override
    public boolean supports(Class<?> type) {
       return ChangePasswordDetails.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object target, Errors errors) {
      ChangePasswordDetails changePasswordDetails = (ChangePasswordDetails) target;

//		Delegate the actual password-field validations.
		validatePasswords(changePasswordDetails, errors);
    }

    private void validatePasswords(ChangePasswordDetails chDetails, Errors errors) {
//		Extract the candidate password value.
		String passWrd = chDetails.getPassword();
		String currentPass = chDetails.getCurrentPassword();

//		Reject if the password is missing/blank and record a field error.
		if (!StringUtils.hasText(passWrd)) {
//			Stop further password validations if it's missing.
			errors.rejectValue(ApiResponseCode.PASSWORD, "password.required", "Password is required");
		}
		
		if(!StringUtils.hasText(currentPass)) {
			errors.rejectValue("currentPassword", "currentPassword.required", "Current Password is required");
		}

//		Proceed to detailed validation and (potentially) encryption of the password.
		validateAndEncrypt(chDetails, errors);
	}

	/**
	 * Validates the password against business rules and, if no errors exist,
	 * encrypts it.
	 * <p>
	 * Workflow:
	 * <ol>
	 * <li>Invoke {@code validatePassword} to run password-strength and consistency
	 * checks (e.g., length, complexity, confirm-password checks).</li>
	 * <li>If the {@link Errors} container has no errors, encrypt the password using
	 * {@link EncryptionService#encrypt(String, String)} and replace the plain text
	 * password in the DTO with the encrypted value.</li>
	 * </ol>
	 * </p>
	 *
	 * @param chpDetails the password change details containing the plain password
	 * @param errors    the validation error container; encryption runs only when
	 *                  there are no errors
	 */
	private void validateAndEncrypt(ChangePasswordDetails chpDetails, Errors errors) {
//		Step 1: Validate the password against defined business rules (length, complexity, etc.).
		validatePassword(chpDetails, errors);

//		Step 2: Continue only if no validation errors were collected.
		if (!errors.hasErrors()) {
//			Step 3: Encrypt the valid password using the configured salt.
			String encPwd = EncryptionService.encrypt(chpDetails.getPassword(), salt);
			String encCurrentPwd = EncryptionService.encrypt(chpDetails.getCurrentPassword(), salt);
			
			Optional<AdminUserValue> userVal = adminUserRepository.findUsersByUserName(chpDetails.getUsername());
			 if(userVal.isPresent()) {
				 if(userVal.get().getPassword().equals(encPwd)) {
					 errors.rejectValue(ApiResponseCode.PASSWORD, "password.mismatch", "The current password and new password cannot be the same. Please choose a different new password.");
				 } else if(!userVal.get().getPassword().equals(encCurrentPwd)) {
					 errors.rejectValue("currentPassword", "currentPassword.mismatch", "Your current password is incorrect. Please enter the correct password.");
				 }
				 else {
					 chpDetails.setPassword(encPwd);
					 chpDetails.setConfirmPassword(encPwd);
				 }
			 }
		}
	}



	/**
	 * Validates the password and confirm‑password fields during a password‑change request.
	 * <p>
	 * This method performs three validations:
	 * <ul>
	 *   <li>Delegates to {@code passwordValidator} to validate password strength and rules.</li>
	 *   <li>Ensures the confirm‑password value is not empty.</li>
	 *   <li>Checks that password and confirm‑password match.</li>
	 * </ul>
	 * If validation fails, appropriate errors are registered in the provided {@link Errors} object.
	 * </p>
	 *
	 * @param chDetails the object containing password and confirm‑password
	 * @param errors    the Spring {@link Errors} object used to record validation failures
	 */
	private void validatePassword(ChangePasswordDetails chDetails, Errors errors) {
	
//	    Validate password strength or format using the passwordValidator component.
	    passwordValidator.validatePassword(chDetails.getPassword(), errors);
	
//	    Ensure that the confirm‑password value is not null or empty.
	    rejectIfEmpty(chDetails.getConfirmPassword(), "confirmPassword", "confirmPassword.required", "Confirm Password is required", errors);
	
//	    Validate that password and confirm‑password match. equalsIgnoreCase() is used here for case‑insensitive comparison.
	    if (!chDetails.getPassword().equalsIgnoreCase(chDetails.getConfirmPassword())) {
	        errors.rejectValue(ApiResponseCode.PASSWORD,  "password.mismatch", "Password and Confirm Password should be the same");
	    }
	}

	/**
	 * Rejects a field if the provided value is null, empty, or contains only whitespace.
	 * <p>
	 * If validation fails, a field‑specific error is added to the {@link Errors} object.
	 * </p>
	 *
	 * @param value   the value to validate
	 * @param field   the field name to associate validation errors with
	 * @param code    the error code used for message resolution
	 * @param message the fallback default message if no message resolver is configured
	 * @param errors  the {@link Errors} object used to register validation failures
	 */
	private void rejectIfEmpty(String value, String field, @NonNull String code, @NonNull String message, Errors errors) {
	
//	    If the provided string is null, empty, or only whitespace, register a validation error.
	    if (!StringUtils.hasText(value)) {
	        errors.rejectValue(field, code, message);
	    }
	}
    
}
