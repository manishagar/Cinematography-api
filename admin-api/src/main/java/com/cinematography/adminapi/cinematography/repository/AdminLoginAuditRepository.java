package com.cinematography.adminapi.cinematography.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinematography.adminapi.cinematography.entities.AdminLoginAuditValue;

public interface AdminLoginAuditRepository extends JpaRepository<AdminLoginAuditValue, Long> {
    
}
