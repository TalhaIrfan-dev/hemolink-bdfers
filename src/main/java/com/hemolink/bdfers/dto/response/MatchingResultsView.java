package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.RequestStage;

import java.util.List;

/**
 * Everything the requester's matching-results page needs (FR-12):
 * request summary, counts, the masked donor list and the active filter.
 */
public class MatchingResultsView {

    private final Long requestId;
    private final String bloodGroupLabel;
    private final String city;
    private final String urgencyLabel;
    private final String adminStatusLabel;
    private final String matchingStatusLabel;
    private final int unitsRequired;
    private final int fulfilledUnits;
    private final boolean refreshAllowed;
    private final String refreshBlockedReason;

    private final long totalMatches;
    private final long awaitingCount;
    private final long acceptedCount;
    private final long declinedCount;
    private final long contactsSharedCount;
    private final long unavailableCount;

    private final DonorResponse filter;
    private final List<MatchSummary> matches;
    private final RequestStage stage;
    private final boolean critical;

    public MatchingResultsView(
            Long requestId,
            String bloodGroupLabel,
            String city,
            String urgencyLabel,
            String adminStatusLabel,
            String matchingStatusLabel,
            int unitsRequired,
            int fulfilledUnits,
            boolean refreshAllowed,
            String refreshBlockedReason,
            long totalMatches,
            long awaitingCount,
            long acceptedCount,
            long declinedCount,
            long contactsSharedCount,
            long unavailableCount,
            DonorResponse filter,
            List<MatchSummary> matches,
            RequestStage stage,
            boolean critical) {

        this.requestId = requestId;
        this.bloodGroupLabel = bloodGroupLabel;
        this.city = city;
        this.urgencyLabel = urgencyLabel;
        this.adminStatusLabel = adminStatusLabel;
        this.matchingStatusLabel = matchingStatusLabel;
        this.unitsRequired = unitsRequired;
        this.fulfilledUnits = fulfilledUnits;
        this.refreshAllowed = refreshAllowed;
        this.refreshBlockedReason = refreshBlockedReason;
        this.totalMatches = totalMatches;
        this.awaitingCount = awaitingCount;
        this.acceptedCount = acceptedCount;
        this.declinedCount = declinedCount;
        this.contactsSharedCount = contactsSharedCount;
        this.unavailableCount = unavailableCount;
        this.filter = filter;
        this.matches = matches;
        this.stage = stage;
        this.critical = critical;
    }

    public Long getRequestId() { return requestId; }
    public String getBloodGroupLabel() { return bloodGroupLabel; }
    public String getCity() { return city; }
    public String getUrgencyLabel() { return urgencyLabel; }
    public String getAdminStatusLabel() { return adminStatusLabel; }
    public String getMatchingStatusLabel() { return matchingStatusLabel; }
    public int getUnitsRequired() { return unitsRequired; }
    public int getFulfilledUnits() { return fulfilledUnits; }
    public boolean isRefreshAllowed() { return refreshAllowed; }
    public String getRefreshBlockedReason() { return refreshBlockedReason; }
    public long getTotalMatches() { return totalMatches; }
    public long getAwaitingCount() { return awaitingCount; }
    public long getAcceptedCount() { return acceptedCount; }
    public long getDeclinedCount() { return declinedCount; }
    public long getContactsSharedCount() { return contactsSharedCount; }

    /** Matched donors whose profiles were deactivated by an administrator. */
    public long getUnavailableCount() { return unavailableCount; }

    /** Active response filter, or null for "All". */
    public DonorResponse getFilter() { return filter; }
    public List<MatchSummary> getMatches() { return matches; }

    /** Combined status shown to the requester (Module 3.3 mapping). */
    public RequestStage getStage() { return stage; }
    public boolean isCritical() { return critical; }

    /** Units donated as a whole-number percentage, for a progress bar. */
    public int getProgressPercent() {
        return unitsRequired <= 0 ? 0 : Math.min(100, fulfilledUnits * 100 / unitsRequired);
    }
}
