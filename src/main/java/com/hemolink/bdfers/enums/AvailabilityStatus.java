package com.hemolink.bdfers.enums;

public enum AvailabilityStatus {
    AVAILABLE("Available"),
    UNAVAILABLE("Unavailable");

    private final String label;

    AvailabilityStatus(String label) {
        this.label = label;
    }

    /** Human-readable label for display in the user interface. */
    public String getLabel() {
        return label;
    }
}
