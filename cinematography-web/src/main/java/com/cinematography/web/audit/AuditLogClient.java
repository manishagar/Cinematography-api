package com.cinematography.web.audit;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.cinematography.core.constants.CommonResponseCodes;
import com.cinematography.core.dto.ApiResponse;
import com.cinematography.core.dto.audit.AuditLogDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Sends one audit record to token-api ({@value CommonResponseCodes#AUDIT_URL}).
 * <p>
 * Failures are logged and swallowed so auditing never affects the API response. Callers stay responsible for
 * running this asynchronously and for mapping their request to the {@link ActionableFields}.
 */
@Slf4j
public class AuditLogClient {

    private final RestTemplate restTemplate;
    private final LoadBalancerClient loadBalancerClient;

    public AuditLogClient(RestTemplate restTemplate, LoadBalancerClient loadBalancerClient) {
        this.restTemplate = restTemplate;
        this.loadBalancerClient = loadBalancerClient;
    }

    /** Identifies who called which contract API, and from where. */
    public record AuditContext(String clientIp, Long customerId, Long contractId, Long contractApiId) {
    }

    /**
     * CI-138 actionable fields, captured regardless of the audit level. Pass null for a field the API does not
     * have; token-api stores it as NULL (shown as NA).
     */
    public record ActionableFields(String importCountryCode, String exportCountryCode, String type,
            String classification) {
    }

    /**
     * @param operation   caller method name, used in log lines (e.g. {@code logAuditDetailsHsLevel})
     * @param apiName     audit API name stored with the record (e.g. {@code tariff})
     * @param context     caller identity
     * @param request     request object; its {@code toString()} is stored when payload auditing is enabled
     * @param apiRes      API response; its data's {@code toString()} is stored when payload auditing is enabled
     * @param isAuditEnabled whether request/response payloads are stored
     * @param fields      actionable fields, or null when the request is null
     */
    public void send(String operation, String apiName, AuditContext context, Object request, ApiResponse<?> apiRes,
            boolean isAuditEnabled, ActionableFields fields) {

        if (log.isDebugEnabled()) {
            log.debug("{} invoked | customerId={} | contractId={} | contractApiId={}",
                    operation, context.customerId(), context.contractId(), context.contractApiId());
        }

        try {
            ServiceInstance serviceInstance = loadBalancerClient.choose(CommonResponseCodes.TOKEN_API);
            if (serviceInstance == null) {
                log.warn("{}: No instance found for token-api", operation);
                return;
            }

            String uri = serviceInstance.getUri().toString();

            AuditLogDetails auditLogDetails = new AuditLogDetails();
            auditLogDetails.setApiName(apiName);
            auditLogDetails.setCustomerId(String.valueOf(context.customerId()));
            auditLogDetails.setContractId(String.valueOf(context.contractId()));
            auditLogDetails.setContractApiId(String.valueOf(context.contractApiId()));
            auditLogDetails.setIpAddress(context.clientIp());
            auditLogDetails.setRequestAt(LocalDateTime.now(ZoneId.systemDefault()));

            if (fields != null) {
                auditLogDetails.setImportCountryCode(fields.importCountryCode());
                auditLogDetails.setExportCountryCode(fields.exportCountryCode());
                auditLogDetails.setType(fields.type());
                auditLogDetails.setClassification(fields.classification());
            }

            if (isAuditEnabled) {
                auditLogDetails.setRequestPayload(request != null ? request.toString() : null);
                if (apiRes != null && apiRes.getData() != null) {
                    auditLogDetails.setResponsePayload(apiRes.getData().toString());
                }
            }

            auditLogDetails.setStatus(CommonResponseCodes.SUCCESS);

            uri = uri + CommonResponseCodes.AUDIT_URL;
            ResponseEntity<Void> response = restTemplate.postForEntity(uri, auditLogDetails, Void.class);

            log.info("{} Response Status ::::: {}", operation, response.getStatusCode());

        } catch (RestClientException e) {
            log.error("{} Response error ::::: {}", operation, e.getMessage(), e);
        }
    }
}
