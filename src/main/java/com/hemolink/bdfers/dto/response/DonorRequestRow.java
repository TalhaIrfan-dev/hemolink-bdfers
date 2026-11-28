package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.enums.DonorRequestTab;

import java.time.LocalDateTime;

/**
 * One matched request as the donor sees it (Module 3.5). Built in the
 * service so the template never walks entity relations (RISK-02) and
 * never sees the requester's identity.
 */
public class DonorRequestRow {

    private final Long matchId;
    private final Long requestId;
    private final String bloodGroupLabel;
    private final int unitsRequired;
    private final int fulfilledUnits;
    private final String city;
    private final String hospitalName;
    private final LocalDateTime requiredAt;
    private final boolean critical;
    private final String urgencyLabel;
    private final String requestStatusLabel;
    private final String responseName;
    private final String responseLabel;
    private final boolean consentGiven;
    private final LocalDateTime consentAt;
    private final LocalDateTime matchedAt;
    private final boolean requestActive;
    private final boolean canRespond;
    private final boolean canShareContact;
    private final boolean canReport;
    private final boolean reported;
    private final DonorRequestTab tab;

    public DonorRequestRow(
            Long matchId,
            Long requestId,
            String bloodGroupLabel,
            int unitsRequired,
            int fulfilledUnits,
            String city,
            String hospitalName,
            LocalDateTime requiredAt,
            boolean critical,
            String urgencyLabel,
            String requestStatusLabel,
            String responseName,
            String responseLabel,
            boolean consentGiven,
            LocalDateTime consentAt,
            LocalDateTime matchedAt,
            boolean requestActive,
            boolean canRespond,
            boolean canShareContact,
            boolean canReport,
            boolean reported,
            DonorRequestTab tab) {
        this.matchId = matchId;
        this.requestId = requestId;
        this.bloodGroupLabel = bloodGroupLabel;
        this.unitsRequired = unitsRequired;
        this.fulfilledUnits = fulfilledUnits;
        this.city = city;
        this.hospitalName = hospitalName;
        this.requiredAt = requiredAt;
        this.critical = critical;
        this.urgencyLabel = urgencyLabel;
        this.requestStatusLabel = requestStatusLabel;
        this.responseName = responseName;
        this.responseLabel = responseLabel;
        this.consentGiven = consentGiven;
        this.consentAt = consentAt;
        this.matchedAt = matchedAt;
        this.requestActive = requestActive;
        this.canRespond = canRespond;
        this.canShareContact = canShareContact;
        this.canReport = canReport;
        this.reported = reported;
        this.tab = tab;
    }

    public Long getMatchId() { return matchId; }
    public Long getRequestId() { return requestId; }
    public String getBloodGroupLabel() { return bloodGroupLabel; }
    public int getUnitsRequired() { return unitsRequired; }
    public int getFulfilledUnits() { return fulfilledUnits; }
    public String getCity() { return city; }
    public String getHospitalName() { return hospitalName; }
    public LocalDateTime getRequiredAt() { return requiredAt; }
    public boolean isCritical() { return critical; }
    public String getUrgencyLabel() { return urgencyLabel; }
    public String getRequestStatusLabel() { return requestStatusLabel; }
    public String getResponseName() { return responseName; }
    public String getResponseLabel() { return responseLabel; }
    public boolean isConsentGiven() { return consentGiven; }
    public LocalDateTime getConsentAt() { return consentAt; }
    public LocalDateTime getMatchedAt() { return matchedAt; }
    public boolean isRequestActive() { return requestActive; }
    public boolean isCanRespond() { return canRespond; }
    public boolean isCanShareContact() { return canShareContact; }
    public boolean isCanReport() { return canReport; }
    public boolean isReported() { return reported; }
    public DonorRequestTab getTab() { return tab; }

    public int getUnitsRemaining() {
        return Math.max(0, unitsRequired - fulfilledUnits);
    }
}
