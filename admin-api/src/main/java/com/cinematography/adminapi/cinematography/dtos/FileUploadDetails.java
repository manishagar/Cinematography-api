package com.cinematography.adminapi.cinematography.dtos;


import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class FileUploadDetails {

        private Long folderId;

    private MultipartFile[] files;
    
}
