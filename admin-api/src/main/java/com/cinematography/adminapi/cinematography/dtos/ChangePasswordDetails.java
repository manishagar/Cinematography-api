package com.cinematography.adminapi.cinematography.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDetails {

    private String username;
    private String currentPassword;
    private String password;
    private String confirmPassword;
    private String salt;
    private String hash;
    private String modifiedDt;

}
