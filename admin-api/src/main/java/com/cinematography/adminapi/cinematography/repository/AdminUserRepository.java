package com.cinematography.adminapi.cinematography.repository;

import com.cinematography.adminapi.cinematography.entities.AdminUserValue;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUserValue, Long> {

     Optional<AdminUserValue> findByUsername(String username);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("SELECT a FROM AdminUserValue a WHERE a.username = :username")
     Optional<AdminUserValue> findByAdminIdForUpdate(@Param("username") String username);

     @Query(value = "SELECT c FROM AdminUserValue c WHERE c.userId = :userId")
     Optional<AdminUserValue> findByUserId(@Param("userId") Long userId);

     @Query("SELECT c FROM AdminUserValue c WHERE c.username = :username AND c.status = 'ACTIVE'")
     Optional<AdminUserValue> findUsersByUserName(@Param("username") String username);

     @Query("""
               SELECT c FROM AdminUserValue c
               WHERE c.status = 'ACTIVE'
               AND c.email IS NOT NULL
               AND c.expiry <= :reminderDate
               AND (
                   c.expiry15DaysEmailSent = false
                   OR c.expiry7DaysEmailSent = false
                   OR c.expiryEmailSent = false
               )
               """)
     List<AdminUserValue> findUsersForPasswordExpiry(
               @Param("reminderDate") LocalDateTime reminderDate);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("""
               SELECT c FROM AdminUserValue c
               WHERE c.userId = :userId
               """)
     Optional<AdminUserValue> findByUserIdForUpdate(
               @Param("userId") Long userId);
}
