package com.cinematography.adminapi.cinematography.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class FolderDetails {

    private Long folderId;

    private Long parentFolderId;

    private String folderName;

    private String description;

    private String languageCode;
}
