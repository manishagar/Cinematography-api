package com.cinematography.adminapi.cinematography.controller;

import org.springframework.stereotype.Component;

import com.cinematography.adminapi.cinematography.repository.AdminUserRepository;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class AdminRepositoriesBundle {

    public final AdminUserRepository adminUserRepository;
    
}
