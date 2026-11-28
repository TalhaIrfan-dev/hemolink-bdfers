package com.hemolink.bdfers.enums;

/**
 * One status a requester understands (Module 3.3 approved mapping), combining
 * the administrator status, urgency and matching status of a blood request:
 * <ul>
 *   <li>Normal + Pending → Awaiting approval</li>
 *   <li>active with matches → Matching</li>
 *   <li>active with no matches → No donors yet</li>
 *   <li>active before the first matching run → Starting</li>
 *   <li>otherwise the final administrator status (Fulfilled, Closed, Rejected)</li>
 * </ul>
 */
public enum RequestStage {

    AWAITING_APPROVAL("Awaiting approval"),
    STARTING("Starting"),
    MATCHING("Matching"),
    NO_DONORS("No donors yet"),
    FULFILLED("Fulfilled"),
    CLOSED("Closed"),
    REJECTED("Rejected");

    private final String label;

    RequestStage(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** True while donors can still respond (Pending or Approved). */
    public boolean isActive() {
        return this == AWAITING_APPROVAL || this == STARTING
                || this == MATCHING || this == NO_DONORS;
    }

    public static RequestStage of(RequestAdminStatus status,
                                  RequestUrgency urgency,
                                  MatchingStatus matching) {
        if (status == null) {
            return STARTING;
        }
        switch (status) {
            case FULFILLED: return FULFILLED;
            case CLOSED: return CLOSED;
            case REJECTED: return REJECTED;
            default: break;
        }
        if (status == RequestAdminStatus.PENDING && urgency == RequestUrgency.NORMAL) {
            return AWAITING_APPROVAL;
        }
        if (matching == MatchingStatus.MATCHES_FOUND) {
            return MATCHING;
        }
        if (matching == MatchingStatus.NO_MATCHES) {
            return NO_DONORS;
        }
        return STARTING;
    }
}
