package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.enums.RequestStage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Requester's view of one of their own blood requests. Contains no donor
 * identity or health data, and no administrator identity.
 */
public class RequestDetailView {

    private final Long requestId;
    private final String bloodGroupLabel;
    private final int unitsRequired;
    private final int fulfilledUnits;
    private final String city;
    private final String hospitalName;
    private final LocalDateTime requiredAt;
    private final String urgencyLabel;
    private final boolean critical;
    private final String adminStatusLabel;
    private final String adminStatusName;
    private final String matchingStatusLabel;
    private final String rejectionReason;
    private final String closeReason;
    private final long totalMatches;
    private final long acceptedCount;
    private final long contactsSharedCount;
    private final boolean closable;
    private final List<TimelineEntry> timeline;
    private final RequestStage stage;

    public RequestDetailView(Long requestId, String bloodGroupLabel, int unitsRequired,
                             int fulfilledUnits, String city, String hospitalName,
                             LocalDateTime requiredAt, String urgencyLabel, boolean critical,
                             String adminStatusLabel, String adminStatusName,
                             String matchingStatusLabel, String rejectionReason,
                             String closeReason, long totalMatches, long acceptedCount,
                             long contactsSharedCount, boolean closable,
                             List<TimelineEntry> timeline, RequestStage stage) {
        this.requestId = requestId;
        this.bloodGroupLabel = bloodGroupLabel;
        this.unitsRequired = unitsRequired;
        this.fulfilledUnits = fulfilledUnits;
        this.city = city;
        this.hospitalName = hospitalName;
        this.requiredAt = requiredAt;
        this.urgencyLabel = urgencyLabel;
        this.critical = critical;
        this.adminStatusLabel = adminStatusLabel;
        this.adminStatusName = adminStatusName;
        this.matchingStatusLabel = matchingStatusLabel;
        this.rejectionReason = rejectionReason;
        this.closeReason = closeReason;
        this.totalMatches = totalMatches;
        this.acceptedCount = acceptedCount;
        this.contactsSharedCount = contactsSharedCount;
        this.closable = closable;
        this.timeline = List.copyOf(timeline);
        this.stage = stage;
    }

    public Long getRequestId() { return requestId; }
    public String getBloodGroupLabel() { return bloodGroupLabel; }
    public int getUnitsRequired() { return unitsRequired; }
    public int getFulfilledUnits() { return fulfilledUnits; }
    public String getCity() { return city; }
    public String getHospitalName() { return hospitalName; }
    public LocalDateTime getRequiredAt() { return requiredAt; }
    public String getUrgencyLabel() { return urgencyLabel; }
    public boolean isCritical() { return critical; }
    public String getAdminStatusLabel() { return adminStatusLabel; }
    public String getAdminStatusName() { return adminStatusName; }
    public String getMatchingStatusLabel() { return matchingStatusLabel; }
    public String getRejectionReason() { return rejectionReason; }
    public String getCloseReason() { return closeReason; }
    public long getTotalMatches() { return totalMatches; }
    public long getAcceptedCount() { return acceptedCount; }
    public long getContactsSharedCount() { return contactsSharedCount; }
    public boolean isClosable() { return closable; }
    public List<TimelineEntry> getTimeline() { return timeline; }

    /** Combined status shown to the requester (Module 3.3 mapping). */
    public RequestStage getStage() { return stage; }

    /** Units donated as a whole-number percentage, for a progress bar. */
    public int getProgressPercent() {
        return unitsRequired <= 0 ? 0 : Math.min(100, fulfilledUnits * 100 / unitsRequired);
    }
}
