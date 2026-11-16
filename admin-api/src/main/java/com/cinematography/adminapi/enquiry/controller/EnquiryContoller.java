package com.cinematography.adminapi.enquiry.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinematography.adminapi.cinematography.dtos.EnquiresRequest;
import com.cinematography.adminapi.cinematography.dtos.EnquiresResponse;
import com.cinematography.adminapi.cinematography.service.EnquiryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestController 
@RequestMapping(value = "/admin/user/admin")
@RequiredArgsConstructor 
public class EnquiryContoller {

    private final EnquiryService enquiryService;

     @PostMapping
    public ResponseEntity<EnquiresResponse> submitEnquiry(
            @Valid @RequestBody EnquiresRequest request) {

        enquiryService.sendEnquiry(request);

        return ResponseEntity.ok(
                new EnquiresResponse("Enquiry submitted successfully"));
    }
    
}
