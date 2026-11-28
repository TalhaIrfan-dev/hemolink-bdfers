package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.entity.DonorStatusChange;

import java.util.List;

/** Everything on the admin donor detail page (D-05, FR-17). */
public class DonorDetailView {

    private final DonorProfile profile;
    private final Advice advice;
    private final List<DonorMatchHistoryRow> history;
    private final List<DonorStatusChange> statusChanges;
    private final long completedDonations;

    public DonorDetailView(DonorProfile profile, Advice advice,
                           List<DonorMatchHistoryRow> history,
                           List<DonorStatusChange> statusChanges,
                           long completedDonations) {
        this.profile = profile;
        this.advice = advice;
        this.history = List.copyOf(history);
        this.statusChanges = List.copyOf(statusChanges);
        this.completedDonations = completedDonations;
    }

    public DonorProfile getProfile() { return profile; }
    public Advice getAdvice() { return advice; }
    public List<DonorMatchHistoryRow> getHistory() { return history; }
    public List<DonorStatusChange> getStatusChanges() { return statusChanges; }
    public long getCompletedDonations() { return completedDonations; }
}
