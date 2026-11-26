package com.cinematography.translationapi.cinematography.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinematography.translationapi.cinematography.entities.TranslationValue;

public interface TranslationRepository extends JpaRepository<TranslationValue, Long> {
    
}
