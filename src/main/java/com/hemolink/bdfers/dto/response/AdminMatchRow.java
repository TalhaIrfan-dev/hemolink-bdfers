package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.entity.DonationRecord;
import com.hemolink.bdfers.entity.DonorHealthCheck;

import java.time.LocalDateTime;

/**
 * One matched donor on the admin request detail page. Administrators may
 * see full donor details and raw health-check values (D-04, D-05).
 */
public class AdminMatchRow {

    private final Long donorProfileId;
    private final String donorName;
    private final String donorEmail;
    private final String donorPhone;
    private final String bloodGroupLabel;
    private final String city;
    private final boolean donorActive;
    private final String responseLabel;
    private final boolean consent;
    private final LocalDateTime consentAt;
    private final DonorHealthCheck healthCheck;
    private final DonationRecord donationRecord;

    public AdminMatchRow(Long donorProfileId, String donorName, String donorEmail, String donorPhone,
                         String bloodGroupLabel, String city, boolean donorActive,
                         String responseLabel, boolean consent, LocalDateTime consentAt,
                         DonorHealthCheck healthCheck, DonationRecord donationRecord) {
        this.donorProfileId = donorProfileId;
        this.donorName = donorName;
        this.donorEmail = donorEmail;
        this.donorPhone = donorPhone;
        this.bloodGroupLabel = bloodGroupLabel;
        this.city = city;
        this.donorActive = donorActive;
        this.responseLabel = responseLabel;
        this.consent = consent;
        this.consentAt = consentAt;
        this.healthCheck = healthCheck;
        this.donationRecord = donationRecord;
    }

    /** Links to the admin donor detail page. */
    public Long getDonorProfileId() { return donorProfileId; }
    public String getDonorName() { return donorName; }
    public String getDonorEmail() { return donorEmail; }
    public String getDonorPhone() { return donorPhone; }
    public String getBloodGroupLabel() { return bloodGroupLabel; }
    public String getCity() { return city; }
    public boolean isDonorActive() { return donorActive; }
    public String getResponseLabel() { return responseLabel; }
    public boolean isConsent() { return consent; }
    public LocalDateTime getConsentAt() { return consentAt; }

    /** Null if the donor has not accepted with a health check. */
    public DonorHealthCheck getHealthCheck() { return healthCheck; }

    /** Null if no donation outcome has been reported. */
    public DonationRecord getDonationRecord() { return donationRecord; }
}
