package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.entity.BloodRequest;

import java.util.List;

/** Admin dashboard data (D-12 cards plus panels). All values are real. */
public class AdminDashboard {

    private final AdminDashboardStats stats;
    private final long pendingThisWeek;
    private final List<LabelCount> requestsByBloodGroup;
    private final List<BloodRequest> criticalQueue;
    private final List<ActivityItem> recentActivity;

    public AdminDashboard(AdminDashboardStats stats, long pendingThisWeek,
                          List<LabelCount> requestsByBloodGroup,
                          List<BloodRequest> criticalQueue,
                          List<ActivityItem> recentActivity) {
        this.stats = stats;
        this.pendingThisWeek = pendingThisWeek;
        this.requestsByBloodGroup = List.copyOf(requestsByBloodGroup);
        this.criticalQueue = List.copyOf(criticalQueue);
        this.recentActivity = List.copyOf(recentActivity);
    }

    public AdminDashboardStats getStats() { return stats; }
    public long getPendingThisWeek() { return pendingThisWeek; }
    public List<LabelCount> getRequestsByBloodGroup() { return requestsByBloodGroup; }
    public List<BloodRequest> getCriticalQueue() { return criticalQueue; }
    public List<ActivityItem> getRecentActivity() { return recentActivity; }

    public long getTotalRequests() {
        return requestsByBloodGroup.stream().mapToLong(LabelCount::getCount).sum();
    }
}
