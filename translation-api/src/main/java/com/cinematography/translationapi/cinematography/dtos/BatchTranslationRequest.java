package com.cinematography.translationapi.cinematography.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BatchTranslationRequest {
    @NotEmpty(message = "Texts are required")
    @Size(max = 50, message = "Maximum 50 texts are allowed")
    private List<
        @NotBlank(message = "Text is required")
        @Size(max = 5000)
        String
    > texts;

    @NotBlank(message = "Source language is required")
    @Size(max = 10)
    private String sourceLanguage = "en";

    @NotBlank(message = "Target language is required")
    @Size(max = 10)
    private String targetLanguage;
}
