package com.cinematography.adminapi.cinematography.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDetails {
    private Long fileId;

    private Long folderId;

    private String fileName;

    private Long fileSize;

    private String contentType;

    private String createdBy;

    private LocalDateTime createdDt;
}
