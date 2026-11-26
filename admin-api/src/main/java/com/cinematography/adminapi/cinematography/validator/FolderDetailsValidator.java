package com.cinematography.adminapi.cinematography.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.cinematography.adminapi.cinematography.dtos.FolderDetails;

@Component
public class FolderDetailsValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {

        return FolderDetails.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        FolderDetails details = (FolderDetails) target;

        if (!StringUtils.hasText(details.getFolderName())) {

            errors.rejectValue(
                    "folderName",
                    "folderName.required",
                    "Folder name is required.");

        } else {

            String folderName = details.getFolderName().trim();

            if (folderName.length() > 255) {

                errors.rejectValue(
                        "folderName",
                        "folderName.length",
                        "Folder name must not exceed 255 characters.");

            } else if (folderName.matches(".*[\\\\/:*?\"<>|\\p{Cntrl}].*")
                    || ".".equals(folderName)
                    || "..".equals(folderName)) {

                errors.rejectValue(
                        "folderName",
                        "folderName.invalid",
                        "Folder name contains invalid characters.");
            }
        }

        if (details.getDescription() != null
                && details.getDescription().length() > 1000) {

            errors.rejectValue(
                    "description",
                    "description.length",
                    "Description must not exceed 1000 characters.");
        }

        if (!StringUtils.hasText(details.getLanguageCode())) {

            errors.rejectValue(
                    "languageCode",
                    "languageCode.required",
                    "Language is required.");
        }
    }
}