package com.cinematography.adminapi.cinematography.entities;

import java.time.LocalDate;
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
@Table(name = "enquiries")
public class EnquiryValue {
     @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String email;

    private String service;

    private LocalDate eventDate;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;
    
}
