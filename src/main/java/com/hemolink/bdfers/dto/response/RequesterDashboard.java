package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.entity.BloodRequest;

import java.util.List;

/**
 * Requester section of the user dashboard (design reference). Every value
 * comes from the database; "this week" means created in the last 7 days.
 * Contains the user's own requests only, and no donor identity.
 */
public class RequesterDashboard {

    private final long activeRequests;
    private final long activeThisWeek;
    private final long potentialMatches;
    private final long matchesThisWeek;
    private final long awaitingResponses;
    private final long awaitingThisWeek;
    private final long completedRequests;
    private final long completedThisWeek;
    private final List<LabelCount> requestsByBloodGroup;
    private final List<BloodRequest> urgentRequests;
    private final List<NotificationView> recentActivity;
    private final List<BloodRequest> recentRequests;

    public RequesterDashboard(long activeRequests, long activeThisWeek,
                              long potentialMatches, long matchesThisWeek,
                              long awaitingResponses, long awaitingThisWeek,
                              long completedRequests, long completedThisWeek,
                              List<LabelCount> requestsByBloodGroup,
                              List<BloodRequest> urgentRequests,
                              List<NotificationView> recentActivity,
                              List<BloodRequest> recentRequests) {
        this.activeRequests = activeRequests;
        this.activeThisWeek = activeThisWeek;
        this.potentialMatches = potentialMatches;
        this.matchesThisWeek = matchesThisWeek;
        this.awaitingResponses = awaitingResponses;
        this.awaitingThisWeek = awaitingThisWeek;
        this.completedRequests = completedRequests;
        this.completedThisWeek = completedThisWeek;
        this.requestsByBloodGroup = List.copyOf(requestsByBloodGroup);
        this.urgentRequests = List.copyOf(urgentRequests);
        this.recentActivity = List.copyOf(recentActivity);
        this.recentRequests = List.copyOf(recentRequests);
    }

    public long getActiveRequests() { return activeRequests; }
    public long getActiveThisWeek() { return activeThisWeek; }
    public long getPotentialMatches() { return potentialMatches; }
    public long getMatchesThisWeek() { return matchesThisWeek; }
    public long getAwaitingResponses() { return awaitingResponses; }
    public long getAwaitingThisWeek() { return awaitingThisWeek; }
    public long getCompletedRequests() { return completedRequests; }
    public long getCompletedThisWeek() { return completedThisWeek; }
    public List<LabelCount> getRequestsByBloodGroup() { return requestsByBloodGroup; }
    public List<BloodRequest> getUrgentRequests() { return urgentRequests; }
    public List<NotificationView> getRecentActivity() { return recentActivity; }
    public List<BloodRequest> getRecentRequests() { return recentRequests; }

    public long getTotalRequests() {
        return requestsByBloodGroup.stream().mapToLong(LabelCount::getCount).sum();
    }
}
