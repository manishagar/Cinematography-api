package com.cinematography.adminapi.cinematography.service;

import com.cinematography.adminapi.cinematography.dtos.EnquiresRequest;

public interface EnquiryService  {

    void sendEnquiry(EnquiresRequest enquiryRequest);
    
}
