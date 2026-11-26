package com.cinematography.adminapi.cinematography.controller;

import org.springframework.stereotype.Component;

import com.cinematography.adminapi.cinematography.validator.BannerDetailsValidator;
import com.cinematography.adminapi.cinematography.validator.ChangePasswordValidator;
import com.cinematography.adminapi.cinematography.validator.ExpiredPasswordValidator;
import com.cinematography.adminapi.cinematography.validator.FolderDetailsValidator;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class AdminValidatorBundle {

    public final BannerDetailsValidator bannerDetailsValidator;

    public final ChangePasswordValidator changePasswordValidator;

    public  final ExpiredPasswordValidator expiredPasswordValidator;

    public final FolderDetailsValidator foldersValidator;
    
}
