package com.cinematography.adminapi.cinematography.controller;

import org.springframework.stereotype.Component;

import com.cinematography.adminapi.cinematography.repository.AdminUserRepository;
import com.cinematography.adminapi.cinematography.repository.FileRepository;
import com.cinematography.adminapi.cinematography.repository.FolderRepository;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class AdminRepositoriesBundle {

    public final AdminUserRepository adminUserRepository;
    public  final FileRepository fileRepository;
    public  final FolderRepository folderRepository;
    
}
