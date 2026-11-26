package com.cinematography.adminapi.cinematography.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Folders")
public class FolderValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "parent_folder_id")
    private Long parentFolderId;

    @Column(name = "folder_name",
            nullable = false,
            columnDefinition = "NVARCHAR(255)")
    private String folderName;

    @Column(name = "description",
            columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "status")
    private String status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "updated_dt")
    private LocalDateTime updatedDt;
}
