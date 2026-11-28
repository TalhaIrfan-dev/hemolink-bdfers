package com.hemolink.bdfers.dto.response;

/**
 * Admin dashboard counts (decision D-12). All values come from the
 * database; nothing is hard-coded.
 */
public class AdminDashboardStats {

    private final long activeDonors;
    private final long availableDonors;
    private final long acceptedAgainstAdviceLast30Days;
    private final long deactivatedDonors;
    private final long pendingBloodRequests;
    private final long criticalNotReviewed;

    public AdminDashboardStats(long activeDonors, long availableDonors,
                               long acceptedAgainstAdviceLast30Days,
                               long deactivatedDonors, long pendingBloodRequests,
                               long criticalNotReviewed) {
        this.activeDonors = activeDonors;
        this.availableDonors = availableDonors;
        this.acceptedAgainstAdviceLast30Days = acceptedAgainstAdviceLast30Days;
        this.deactivatedDonors = deactivatedDonors;
        this.pendingBloodRequests = pendingBloodRequests;
        this.criticalNotReviewed = criticalNotReviewed;
    }

    public long getActiveDonors() { return activeDonors; }
    public long getAvailableDonors() { return availableDonors; }
    public long getAcceptedAgainstAdviceLast30Days() { return acceptedAgainstAdviceLast30Days; }
    public long getDeactivatedDonors() { return deactivatedDonors; }
    public long getPendingBloodRequests() { return pendingBloodRequests; }
    public long getCriticalNotReviewed() { return criticalNotReviewed; }
}
