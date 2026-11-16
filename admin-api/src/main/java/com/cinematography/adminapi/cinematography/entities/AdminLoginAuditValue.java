package com.cinematography.adminapi.cinematography.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@Entity 
@NoArgsConstructor 
@AllArgsConstructor 
@Table(name = "Admin_Login_Audit")
public class AdminLoginAuditValue {

     @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "admin_id")
    private Long adminId;

    private String username;

    @Column(name = "event_type")
    private String eventType;

    private String status;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
}
