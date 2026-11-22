package com.cinematography.adminapi.cinematography.validator;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.cinematography.adminapi.cinematography.dtos.BannerDetails;
import com.cinematography.core.util.DateUtils;
import com.cinematography.cust.util.AuthUtilsEnum;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data 
@Slf4j
@Component
@RequiredArgsConstructor 
public class BannerDetailsValidator implements Validator {

    private static final String START_AT = "startsAt";
    private static final String END_AT = "endsAt";

    @Override
    public boolean supports(Class<?> type) {
        return BannerDetails.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BannerDetails details = (BannerDetails) target;

        if (details.getBannerType() != null && !"TEXT".equals(details.getBannerType())) {
            errors.rejectValue("bannerType", "banner.type", "Select Write Text for this endpoint.");
        }
        String title = details.getTitle();
        validateField("title", details.getTitle(), 200, true, errors);
                validateField("subtitle", details.getSubtitle(),1000, false, errors);
        
        validateColor(details.getBackgroundColor(), "backgroundColor", errors);
        validateColor(details.getTextColor(), "textColor", errors);

        String start = details.getStartsAt();
        String end = details.getEndsAt();
        validateDateField(start , START_AT, false, errors);
        validateDateField(end, END_AT, false, errors);
    }

    private void validateColor(String color, String field, Errors errors) {
        if (color != null && !color.matches("#[0-9a-fA-F]{6}")) {
            errors.rejectValue(field, "banner.color", "Use a six-digit hex colour.");
        }
    }



    	private void validateField(String fieldName, String fieldValue, int maxLength, boolean required, Errors errors) {
//	    Check if the field is required but missing or empty , Check if the field value exceeds the maximum allowed length
		if (required && !StringUtils.hasText(fieldValue)) {
			errors.rejectValue(fieldName, fieldName + ".required", fieldName + " is required");
		} else if (StringUtils.hasText(fieldValue) && fieldValue.length() > maxLength) {
			errors.rejectValue(fieldName, fieldName + ".length",
					fieldName + " should not exceed " + maxLength + " characters");
		}
	}

    	private Optional<LocalDateTime> validateDateField(String dateStr, String field, boolean required, Errors errors) {
		if (!StringUtils.hasText(dateStr)) {
			if (required) {
				errors.rejectValue(field, field + ".required", field + " is required");
			}
			return Optional.empty();
		}

		try {
//	        Use new UTC parser instead of old ISO_LOCAL_DATE_TIME
			Optional<LocalDateTime> dateTimeOpt = DateUtils.formatUtcToLocalDateTime(dateStr,
					AuthUtilsEnum.Constants.UTC_DATE_TIME_FORMAT);

			if (dateTimeOpt.isEmpty()) {
				errors.rejectValue(field, field + ".invalid",
						"Invalid date format. Please provide in UTC ISO format yyyy-MM-dd'T'HH:mm:ss'Z'");
			}

			return dateTimeOpt;

		} catch (Exception e) {
//	        Parsing failure handling
			log.error("Error parsing {}: {}", field, e.getMessage());
			errors.rejectValue(field, field + ".invalid", "Error in parsing date");
			return Optional.empty();
		}
	}
}
