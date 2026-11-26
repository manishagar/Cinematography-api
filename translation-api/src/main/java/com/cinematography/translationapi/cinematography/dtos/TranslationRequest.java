package com.cinematography.translationapi.cinematography.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data 
@Builder 
@AllArgsConstructor
public class TranslationRequest {
     @NotBlank(message = "Text is required")
    @Size(
        max = 5000,
        message = "Text must not exceed 5000 characters"
    )
    private String text;

    @NotBlank(message = "Source language is required")
    @Size(max = 10)
    private String sourceLanguage = "en";

    @NotBlank(message = "Target language is required")
    @Size(max = 10)
    private String targetLanguage;
}
