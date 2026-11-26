package com.cinematography.adminapi.cinematography.repository;


import com.cinematography.adminapi.cinematography.entities.FolderValue;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface FolderRepository
        extends JpaRepository<FolderValue, Long> {

    @Query("""
            SELECT f FROM FolderValue f
            WHERE f.folderId = :folderId
            AND f.status = 'ACTIVE'
            """)
    Optional<FolderValue> findByFolderId(
            @Param("folderId") Long folderId);

    @Query("""
            SELECT f FROM FolderValue f
            WHERE f.parentFolderId IS NULL
            AND f.status = 'ACTIVE'
            ORDER BY f.folderName
            """)
    List<FolderValue> findRootFolders();

    @Query("""
            SELECT f FROM FolderValue f
            WHERE f.parentFolderId = :parentFolderId
            AND f.status = 'ACTIVE'
            ORDER BY f.folderName
            """)
    List<FolderValue> findSubFolders(
            @Param("parentFolderId") Long parentFolderId);

    @Query("""
            SELECT f FROM FolderValue f
            WHERE f.status = 'ACTIVE'
            ORDER BY f.folderName
            """)
    List<FolderValue> findActiveFolders();

    @Query("""
            SELECT COUNT(f) FROM FolderValue f
            WHERE LOWER(f.folderName) = LOWER(:folderName)
            AND f.status = 'ACTIVE'
            AND (
                f.parentFolderId = :parentFolderId
                OR (
                    f.parentFolderId IS NULL
                    AND :parentFolderId IS NULL
                )
            )
            """)
    Long countByFolderName(
            @Param("folderName") String folderName,
            @Param("parentFolderId") Long parentFolderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f FROM FolderValue f
            WHERE f.folderId = :folderId
            AND f.status = 'ACTIVE'
            """)
    Optional<FolderValue> findByFolderIdForUpdate(
            @Param("folderId") Long folderId);

    @Query("""
            SELECT f FROM FolderValue f
            WHERE f.status = 'ACTIVE'
            AND LOWER(f.folderName)
                LIKE LOWER(CONCAT('%', :searchText, '%'))
            """)
    List<FolderValue> searchFolders(
            @Param("searchText") String searchText);

    @Query("""
            SELECT COUNT(f) FROM FolderValue f
            WHERE f.status = 'ACTIVE'
            """)
    Long countActiveFolders();
}