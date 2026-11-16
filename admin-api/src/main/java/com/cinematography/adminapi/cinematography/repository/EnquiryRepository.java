package com.cinematography.adminapi.cinematography.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinematography.adminapi.cinematography.entities.EnquiryValue;

public interface EnquiryRepository extends JpaRepository<EnquiryValue, Long>{
    
}
