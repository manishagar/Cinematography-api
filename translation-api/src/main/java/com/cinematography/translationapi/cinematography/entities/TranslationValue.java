package com.cinematography.translationapi.cinematography.entities;

import java.time.Clock;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Nationalized;

@Data 
@Builder
@Entity 
@NoArgsConstructor 
@AllArgsConstructor 
@Table(name = "enquiries")
public class TranslationValue {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "translation_id")
    private Long translationId;

    @Nationalized
    @Column(
        name = "original_text",
        nullable = false,
        columnDefinition = "NVARCHAR(MAX)"
    )
    private String originalText;

    @Column(
        name = "source_hash",
        nullable = false,
        length = 64
    )
    private String sourceHash;

    @Column(
        name = "source_language",
        nullable = false,
        length = 10
    )
    private String sourceLanguage;

    @Column(
        name = "target_language",
        nullable = false,
        length = 10
    )
    private String targetLanguage;

    @Nationalized
    @Column(
        name = "translated_text",
        nullable = false,
        columnDefinition = "NVARCHAR(MAX)"
    )
    private String translatedText;

    @Column(
        name = "translation_provider",
        nullable = false,
        length = 30
    )
    private String translationProvider;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        LocalDateTime now =
            LocalDateTime.now(Clock.systemUTC());

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {

        updatedAt =
            LocalDateTime.now(Clock.systemUTC());
    }
    
}
