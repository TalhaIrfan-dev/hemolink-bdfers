package com.hemolink.bdfers.enums;

public enum RequestAdminStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CLOSED("Closed"),
    FULFILLED("Fulfilled");

    private final String label;

    RequestAdminStatus(String label) {
        this.label = label;
    }

    /** Human-readable label for display in the user interface. */
    public String getLabel() {
        return label;
    }
}
