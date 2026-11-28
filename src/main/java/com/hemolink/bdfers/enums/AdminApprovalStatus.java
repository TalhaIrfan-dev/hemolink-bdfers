package com.hemolink.bdfers.enums;

public enum AdminApprovalStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    REVOKED("Revoked");

    private final String label;

    AdminApprovalStatus(String label) {
        this.label = label;
    }

    /** Human-readable label for display in the user interface. */
    public String getLabel() {
        return label;
    }
}
