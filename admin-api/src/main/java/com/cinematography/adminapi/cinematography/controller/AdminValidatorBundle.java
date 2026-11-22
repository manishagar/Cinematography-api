package com.cinematography.adminapi.cinematography.controller;

import org.springframework.stereotype.Component;

import com.cinematography.adminapi.cinematography.validator.BannerDetailsValidator;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class AdminValidatorBundle {

    public final BannerDetailsValidator bannerDetailsValidator;
    
}
