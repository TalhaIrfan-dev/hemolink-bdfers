package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.entity.BloodRequest;

import java.util.List;

/** Everything on the admin blood request detail page (FR-16). */
public class AdminRequestDetailView {

    private final BloodRequest request;
    private final List<AdminMatchRow> matches;
    private final boolean reviewable;
    private final boolean closable;
    private final boolean criticalNotReviewed;

    public AdminRequestDetailView(BloodRequest request, List<AdminMatchRow> matches,
                                  boolean reviewable, boolean closable,
                                  boolean criticalNotReviewed) {
        this.request = request;
        this.matches = List.copyOf(matches);
        this.reviewable = reviewable;
        this.closable = closable;
        this.criticalNotReviewed = criticalNotReviewed;
    }

    public BloodRequest getRequest() { return request; }
    public List<AdminMatchRow> getMatches() { return matches; }

    /** True while the request is Pending (approve or reject allowed). */
    public boolean isReviewable() { return reviewable; }

    /** True while the request is Pending or Approved (close allowed). */
    public boolean isClosable() { return closable; }

    /** Critical request that no administrator has reviewed yet (D-15). */
    public boolean isCriticalNotReviewed() { return criticalNotReviewed; }
}
