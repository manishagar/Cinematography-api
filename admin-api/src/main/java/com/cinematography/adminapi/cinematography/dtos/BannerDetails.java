package com.cinematography.adminapi.cinematography.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class BannerDetails {
    private String bannerId;
    private String bannerType;
    private String title;
    private String subtitle;
    private String altText;
    private String backgroundColor;
    private String textColor;
    private Boolean active;
    private String startsAt;
    private String endsAt;
    private String createdBy;
    private String createdt;
    private String updatedBy;
    private String updaatedAt;

}
