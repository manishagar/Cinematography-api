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
@Table(name = "Admin_Revoked_Tokens")
public class AdminRevokedTokenValue {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "revoked_token_id")
    private Long revokedTokenId;

    @Column(name = "token_id")
    private String tokenId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
}
