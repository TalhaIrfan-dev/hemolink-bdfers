package com.hemolink.bdfers.service;

import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.exception.BusinessRuleException;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Single source of truth for blood-request administrative status rules
 * (SRS FR-19).
 *
 * Permitted transitions:
 * <pre>
 *   PENDING  -> APPROVED, REJECTED, CLOSED
 *   APPROVED -> FULFILLED, CLOSED
 *   REJECTED, CLOSED, FULFILLED -> (final, no further changes)
 *
 *   Additionally, for CRITICAL requests only (decision D-15):
 *   PENDING  -> FULFILLED   (admin review of Critical requests is optional)
 * </pre>
 *
 * A request is "active" while it is PENDING or APPROVED. Only active
 * requests may be matched, responded to, have contacts disclosed or
 * receive donation reports.
 */
public final class RequestStatusPolicy {

    private static final Map<RequestAdminStatus, Set<RequestAdminStatus>> ALLOWED =
            Map.of(
                    RequestAdminStatus.PENDING, EnumSet.of(
                            RequestAdminStatus.APPROVED,
                            RequestAdminStatus.REJECTED,
                            RequestAdminStatus.CLOSED),
                    RequestAdminStatus.APPROVED, EnumSet.of(
                            RequestAdminStatus.FULFILLED,
                            RequestAdminStatus.CLOSED),
                    RequestAdminStatus.REJECTED, EnumSet.noneOf(RequestAdminStatus.class),
                    RequestAdminStatus.CLOSED, EnumSet.noneOf(RequestAdminStatus.class),
                    RequestAdminStatus.FULFILLED, EnumSet.noneOf(RequestAdminStatus.class)
            );

    private RequestStatusPolicy() {
    }

    /** True while the request is PENDING or APPROVED. */
    public static boolean isActive(RequestAdminStatus status) {
        return status == RequestAdminStatus.PENDING
                || status == RequestAdminStatus.APPROVED;
    }

    /** True when the status is REJECTED, CLOSED or FULFILLED. */
    public static boolean isFinal(RequestAdminStatus status) {
        return !isActive(status);
    }

    /** True if FR-19 permits changing from {@code from} to {@code to}. */
    public static boolean canTransition(
            RequestAdminStatus from, RequestAdminStatus to) {

        if (from == null || to == null) {
            return false;
        }
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    /**
     * Same as {@link #canTransition(RequestAdminStatus, RequestAdminStatus)}
     * but also applies the Critical-request rule: a CRITICAL request may go
     * from PENDING directly to FULFILLED (D-15).
     */
    public static boolean canTransition(
            RequestAdminStatus from,
            RequestAdminStatus to,
            RequestUrgency urgency) {

        if (urgency == RequestUrgency.CRITICAL
                && from == RequestAdminStatus.PENDING
                && to == RequestAdminStatus.FULFILLED) {
            return true;
        }
        return canTransition(from, to);
    }

    /**
     * Urgency-aware version of
     * {@link #requireTransition(RequestAdminStatus, RequestAdminStatus)}.
     */
    public static void requireTransition(
            RequestAdminStatus from,
            RequestAdminStatus to,
            RequestUrgency urgency) {

        if (!canTransition(from, to, urgency)) {
            throw new BusinessRuleException(
                    "This request cannot be changed from "
                    + label(from) + " to " + label(to) + ".");
        }
    }

    /**
     * True if donors may respond to, consent on, or report donations for a
     * request (D-15): CRITICAL requests while PENDING or APPROVED, NORMAL
     * requests only once APPROVED.
     */
    public static boolean acceptsDonorActions(
            RequestAdminStatus status, RequestUrgency urgency) {

        if (urgency == RequestUrgency.CRITICAL) {
            return isActive(status);
        }
        return status == RequestAdminStatus.APPROVED;
    }

    /**
     * Throws a {@link BusinessRuleException} if donors may not act on the
     * request in its current status.
     */
    public static void requireDonorActionsAllowed(
            RequestAdminStatus status, RequestUrgency urgency) {

        if (acceptsDonorActions(status, urgency)) {
            return;
        }
        if (status == RequestAdminStatus.REJECTED) {
            throw new BusinessRuleException(
                    "This blood request was rejected by an administrator "
                    + "and can no longer be accepted.");
        }
        if (status == RequestAdminStatus.PENDING) {
            throw new BusinessRuleException(
                    "This request is awaiting administrator approval.");
        }
        throw new BusinessRuleException(
                "This blood request is no longer active.");
    }

    /**
     * Throws a {@link BusinessRuleException} with a user-friendly message
     * if the transition is not permitted.
     */
    public static void requireTransition(
            RequestAdminStatus from, RequestAdminStatus to) {

        if (!canTransition(from, to)) {
            throw new BusinessRuleException(
                    "This request cannot be changed from "
                    + label(from) + " to " + label(to) + ".");
        }
    }

    /**
     * Throws a {@link BusinessRuleException} with the given message if
     * the request is no longer active.
     */
    public static void requireActive(
            RequestAdminStatus status, String message) {

        if (!isActive(status)) {
            throw new BusinessRuleException(message);
        }
    }

    private static String label(RequestAdminStatus status) {
        return status == null ? "Unknown" : status.getLabel();
    }
}
