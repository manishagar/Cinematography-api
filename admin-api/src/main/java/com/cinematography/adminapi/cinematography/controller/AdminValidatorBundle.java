package com.cinematography.adminapi.cinematography.controller;

import org.springframework.stereotype.Component;

import com.cinematography.adminapi.cinematography.validator.BannerDetailsValidator;
import com.cinematography.adminapi.cinematography.validator.ChangePasswordValidator;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class AdminValidatorBundle {

    public final BannerDetailsValidator bannerDetailsValidator;

    public final ChangePasswordValidator changePasswordValidator;
    
}
