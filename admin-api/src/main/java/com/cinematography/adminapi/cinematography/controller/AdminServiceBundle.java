package com.cinematography.adminapi.cinematography.controller;

import org.springframework.stereotype.Component;

import com.cinematography.adminapi.cinematography.service.AdminService;
import com.cinematography.cust.service.JwtService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminServiceBundle {

    public final AdminService adminService;

    public final JwtService jwtService;

}
