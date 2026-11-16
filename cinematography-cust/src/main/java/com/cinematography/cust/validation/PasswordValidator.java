package com.cinematography.cust.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;

@Configuration
public class PasswordValidator {

	private static final int MAX_PASSWORD_LENGTH = 50;

	private static final String PASS_WORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])(?=\\S+$).{8,}$";

	private Pattern pattern;
	
	private static final String PASS_WORD = "password";

	public PasswordValidator() {
		pattern = Pattern.compile(PASS_WORD_REGEX);
	}

	public void validatePassword(String password, Errors errors) {
		if (ObjectUtils.isEmpty(password)) {
			errors.rejectValue(PASS_WORD, "password.required", "Password is required");
		} else {
//			check min & max length
			if (password.length() > MAX_PASSWORD_LENGTH) {
				errors.rejectValue(PASS_WORD, "password.max.length", "Password is too long: max number of chars is : " + MAX_PASSWORD_LENGTH);
			} 

//			Password must to be at least 8 chars, 1 number, 1 upper case, 1 lower case letter, 1 special char, no spaces
			Matcher matcher = pattern.matcher(password);
			if (!matcher.matches()) {
				errors.rejectValue(PASS_WORD, "password.validation", "Password must be at least 8 chars, 1 number, 1 upper case," + " 1 lower case letter, 1 special char, no spaces");
			}
		}
	}

}
