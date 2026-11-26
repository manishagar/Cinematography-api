package com.cinematography.adminapi.cinematography.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpiredPasswordChangeRequest {
    private String username;
    private String currentPassword;
    private String password;
    private String confirmPassword;
}
