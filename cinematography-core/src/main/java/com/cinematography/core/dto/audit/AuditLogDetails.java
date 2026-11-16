package com.cinematography.core.dto.audit;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuditLogDetails {

    private String apiName;
    private String customerId;
    private String contractId;
    private String contractApiId;
    private String ipAddress;
    private LocalDateTime requestAt;
    private String importCountryCode;
    private String exportCountryCode;
    private String type;
    private String classification;
    private String requestPayload;
    private String responsePayload;
    private String status;
}
