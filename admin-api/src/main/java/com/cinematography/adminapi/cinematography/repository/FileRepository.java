package com.cinematography.adminapi.cinematography.repository;



import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.cinematography.adminapi.cinematography.entities.FileValue;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface FileRepository
        extends JpaRepository<FileValue, Long> {

    @Query("""
            SELECT f FROM FileValue f
            WHERE f.fileId = :fileId
            AND f.status = 'ACTIVE'
            """)
    Optional<FileValue> findByFileId(
            @Param("fileId") Long fileId);

    @Query("""
            SELECT f FROM FileValue f
            WHERE f.folderId = :folderId
            AND f.status = 'ACTIVE'
            ORDER BY f.createdDt DESC
            """)
    List<FileValue> findFilesByFolderId(
            @Param("folderId") Long folderId);

    @Query("""
            SELECT f FROM FileValue f
            WHERE f.folderId IS NULL
            AND f.status = 'ACTIVE'
            ORDER BY f.createdDt DESC
            """)
    List<FileValue> findRootFiles();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f FROM FileValue f
            WHERE f.fileId = :fileId
            AND f.status = 'ACTIVE'
            """)
    Optional<FileValue> findByFileIdForUpdate(
            @Param("fileId") Long fileId);

    @Query("""
            SELECT COUNT(f) FROM FileValue f
            WHERE f.status = 'ACTIVE'
            AND f.folderId = :folderId
            """)
    Long countFilesByFolderId(
            @Param("folderId") Long folderId);

    @Query("""
            SELECT f FROM FileValue f
            WHERE f.status = 'ACTIVE'
            AND LOWER(f.fileName)
                LIKE LOWER(CONCAT('%', :searchText, '%'))
            """)
    List<FileValue> searchFiles(
            @Param("searchText") String searchText);

    @Query("""
            SELECT COUNT(f) FROM FileValue f
            WHERE f.status = 'ACTIVE'
            """)
    Long countActiveFiles();

    @Query("""
            SELECT COALESCE(SUM(f.fileSize), 0)
            FROM FileValue f
            WHERE f.status = 'ACTIVE'
            """)
    Long getTotalStorageUsed();
}
