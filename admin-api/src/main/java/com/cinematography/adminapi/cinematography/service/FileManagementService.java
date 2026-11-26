package com.cinematography.adminapi.cinematography.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cinematography.adminapi.cinematography.dtos.FileDetails;
import com.cinematography.adminapi.cinematography.dtos.FolderContentDetails;
import com.cinematography.adminapi.cinematography.dtos.FolderDetails;
import com.cinematography.adminapi.cinematography.entities.FileValue;
import com.cinematography.adminapi.cinematography.entities.FolderValue;
import com.cinematography.adminapi.cinematography.repository.FileRepository;
import com.cinematography.adminapi.cinematography.repository.FolderRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor 
public class FileManagementService {

    private final FolderRepository folderRepository;

    private final FileRepository fileRepository;

    @Transactional
    public FolderDetails createFolder(
            FolderDetails details, String username) {

        return addFolderDetails(details, username);
    }

    private FolderDetails addFolderDetails(
            FolderDetails details, String username) {

        log.info("FileManagementService addFolderDetails invoked");

        if (details == null
                || !StringUtils.hasText(details.getFolderName())) {

            throw new IllegalArgumentException(
                    "folderName: Folder name is required.");
        }

        String folderName = details.getFolderName().trim();

        if (details.getParentFolderId() != null) {

            folderRepository.findByFolderIdForUpdate(
                    details.getParentFolderId())

                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "parentFolderId: Parent folder not found."));
        }

        Long count = folderRepository.countByFolderName(
                folderName,
                details.getParentFolderId());

        if (count > 0) {

            throw new IllegalArgumentException(
                    "folderName: Folder already exists.");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        FolderValue folder = new FolderValue();

        folder.setFolderName(folderName);
        folder.setParentFolderId(details.getParentFolderId());
        folder.setDescription(details.getDescription());
        folder.setLanguageCode(details.getLanguageCode());
        folder.setStatus("ACTIVE");
        folder.setCreatedBy(username);
        folder.setCreatedDt(now);
        folder.setModifiedBy(username);
        folder.setUpdatedDt(now);

        FolderValue savedFolder = folderRepository.save(folder);

        return mapFolderDetails(savedFolder);
    }

    @Transactional(readOnly = true)
    public List<FolderDetails> getAllFolders() {

        return folderRepository.findActiveFolders()
                .stream()
                .map(this::mapFolderDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public FolderContentDetails getFolderContents(Long folderId) {

        FolderContentDetails details = new FolderContentDetails();

        List<FolderValue> folders;
        List<FileValue> files;

        if (folderId == null) {

            details.setFolderName("Root");

            folders = folderRepository.findRootFolders();
            files = fileRepository.findRootFiles();

        } else {

            FolderValue folder = folderRepository
                    .findByFolderId(folderId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Folder not found."));

            details.setFolderId(folder.getFolderId());
            details.setFolderName(folder.getFolderName());

            folders = folderRepository.findSubFolders(folderId);

            files = fileRepository.findFilesByFolderId(folderId);
        }

        details.setFolders(
                folders.stream()
                        .map(this::mapFolderDetails)
                        .toList());

        details.setFiles(
                files.stream()
                        .map(this::mapFileDetails)
                        .toList());

        details.setTotalFolders((long) folders.size());

        details.setTotalFiles((long) files.size());

        return details;
    }

    private FolderDetails mapFolderDetails(FolderValue folder) {

        FolderDetails details = new FolderDetails();

        details.setFolderId(folder.getFolderId());
        details.setParentFolderId(folder.getParentFolderId());
        details.setFolderName(folder.getFolderName());
        details.setDescription(folder.getDescription());
        details.setLanguageCode(folder.getLanguageCode());

        return details;
    }

    private FileDetails mapFileDetails(FileValue file) {

        FileDetails details = new FileDetails();

        details.setFileId(file.getFileId());
        details.setFolderId(file.getFolderId());
        details.setFileName(file.getFileName());
        details.setFileSize(file.getFileSize());
        details.setContentType(file.getContentType());
        details.setCreatedBy(file.getCreatedBy());
        details.setCreatedDt(file.getCreatedDt());

        return details;
    }
}