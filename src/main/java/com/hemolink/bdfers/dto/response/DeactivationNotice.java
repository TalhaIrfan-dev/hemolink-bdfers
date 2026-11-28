package com.hemolink.bdfers.dto.response;

import java.time.LocalDateTime;

/**
 * Shown to a donor whose profile was deactivated by an administrator
 * (decision: tell the donor why). Contains no administrator identity.
 */
public class DeactivationNotice {

    private final String reason;
    private final LocalDateTime deactivatedAt;

    public DeactivationNotice(String reason, LocalDateTime deactivatedAt) {
        this.reason = reason;
        this.deactivatedAt = deactivatedAt;
    }

    /** May be null for profiles deactivated before history was recorded. */
    public String getReason() { return reason; }
    public LocalDateTime getDeactivatedAt() { return deactivatedAt; }
}
