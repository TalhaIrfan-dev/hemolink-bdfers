package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.entity.DonationRecord;
import com.hemolink.bdfers.entity.DonorHealthCheck;

import java.time.LocalDateTime;

/** One request a donor was matched to (admin donor detail page). */
public class DonorMatchHistoryRow {

    private final Long requestId;
    private final String requestBloodGroupLabel;
    private final String hospitalName;
    private final String city;
    private final String urgencyLabel;
    private final String requestStatusLabel;
    private final String responseLabel;
    private final boolean consent;
    private final LocalDateTime consentAt;
    private final DonorHealthCheck healthCheck;
    private final DonationRecord donationRecord;

    public DonorMatchHistoryRow(Long requestId, String requestBloodGroupLabel,
                                String hospitalName, String city, String urgencyLabel,
                                String requestStatusLabel, String responseLabel,
                                boolean consent, LocalDateTime consentAt,
                                DonorHealthCheck healthCheck, DonationRecord donationRecord) {
        this.requestId = requestId;
        this.requestBloodGroupLabel = requestBloodGroupLabel;
        this.hospitalName = hospitalName;
        this.city = city;
        this.urgencyLabel = urgencyLabel;
        this.requestStatusLabel = requestStatusLabel;
        this.responseLabel = responseLabel;
        this.consent = consent;
        this.consentAt = consentAt;
        this.healthCheck = healthCheck;
        this.donationRecord = donationRecord;
    }

    public Long getRequestId() { return requestId; }
    public String getRequestBloodGroupLabel() { return requestBloodGroupLabel; }
    public String getHospitalName() { return hospitalName; }
    public String getCity() { return city; }
    public String getUrgencyLabel() { return urgencyLabel; }
    public String getRequestStatusLabel() { return requestStatusLabel; }
    public String getResponseLabel() { return responseLabel; }
    public boolean isConsent() { return consent; }
    public LocalDateTime getConsentAt() { return consentAt; }
    public DonorHealthCheck getHealthCheck() { return healthCheck; }
    public DonationRecord getDonationRecord() { return donationRecord; }
}
