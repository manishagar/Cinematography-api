package com.cinematography.adminapi.cinematography.controller;

import org.springframework.stereotype.Component;

import com.cinematography.adminapi.cinematography.util.JwtUtil;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
@Getter 
public class AdminInfraBundle {

    private final JwtUtil jwtUtil;
    
}
