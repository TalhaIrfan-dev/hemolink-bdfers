package com.hemolink.bdfers.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Donor section of the user dashboard. Null fields mean "no donor profile". */
public class DonorDashboard {

    /** A request waiting for this donor's reply. */
    public static class PendingItem {
        private final Long matchId;
        private final Long requestId;
        private final String bloodGroupLabel;
        private final String hospitalName;
        private final String city;
        private final String urgencyLabel;
        private final boolean critical;

        public PendingItem(Long matchId, Long requestId, String bloodGroupLabel,
                           String hospitalName, String city, String urgencyLabel,
                           boolean critical) {
            this.matchId = matchId;
            this.requestId = requestId;
            this.bloodGroupLabel = bloodGroupLabel;
            this.hospitalName = hospitalName;
            this.city = city;
            this.urgencyLabel = urgencyLabel;
            this.critical = critical;
        }

        public Long getMatchId() { return matchId; }
        public Long getRequestId() { return requestId; }
        public String getBloodGroupLabel() { return bloodGroupLabel; }
        public String getHospitalName() { return hospitalName; }
        public String getCity() { return city; }
        public String getUrgencyLabel() { return urgencyLabel; }
        public boolean isCritical() { return critical; }
    }

    private final boolean hasProfile;
    private final boolean profileActive;
    private final String availabilityLabel;
    private final Advice advice;
    private final long awaitingMyReply;
    private final List<PendingItem> pendingItems;
    private final long acceptedActive;
    private final long completedDonations;
    private final LocalDate lastDonationDate;
    private final LocalDate nextSuggestedDate;

    public DonorDashboard(boolean hasProfile, boolean profileActive, String availabilityLabel,
                          Advice advice, long awaitingMyReply, List<PendingItem> pendingItems,
                          long acceptedActive, long completedDonations,
                          LocalDate lastDonationDate, LocalDate nextSuggestedDate) {
        this.hasProfile = hasProfile;
        this.profileActive = profileActive;
        this.availabilityLabel = availabilityLabel;
        this.advice = advice;
        this.awaitingMyReply = awaitingMyReply;
        this.pendingItems = List.copyOf(pendingItems);
        this.acceptedActive = acceptedActive;
        this.completedDonations = completedDonations;
        this.lastDonationDate = lastDonationDate;
        this.nextSuggestedDate = nextSuggestedDate;
    }

    public static DonorDashboard noProfile() {
        return new DonorDashboard(false, false, null, null, 0, List.of(), 0, 0, null, null);
    }

    public boolean isHasProfile() { return hasProfile; }
    public boolean isProfileActive() { return profileActive; }
    public String getAvailabilityLabel() { return availabilityLabel; }
    public Advice getAdvice() { return advice; }
    public long getAwaitingMyReply() { return awaitingMyReply; }
    public List<PendingItem> getPendingItems() { return pendingItems; }
    public long getAcceptedActive() { return acceptedActive; }
    public long getCompletedDonations() { return completedDonations; }
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public LocalDate getNextSuggestedDate() { return nextSuggestedDate; }
}
