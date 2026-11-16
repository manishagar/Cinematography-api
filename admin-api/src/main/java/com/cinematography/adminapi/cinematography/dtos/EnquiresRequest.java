package com.cinematography.adminapi.cinematography.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data 
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EnquiresRequest {
    
      @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String service;

    private String eventDate;

    @NotBlank
    private String message;
}
