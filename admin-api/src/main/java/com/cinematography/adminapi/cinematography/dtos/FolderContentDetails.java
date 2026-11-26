package com.cinematography.adminapi.cinematography.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class FolderContentDetails {
      private Long folderId;

    private String folderName;

    private List<FolderDetails> folders;

    private List<FileDetails> files;

    private Long totalFolders;

    private Long totalFiles;
    
}
