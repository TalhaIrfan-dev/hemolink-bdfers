package com.hemolink.bdfers.enums;

public enum EligibilityStatus {
    ELIGIBLE("Eligible"),
    INELIGIBLE("Ineligible"),
    REQUIRES_REVIEW("Requires review");

    private final String label;

    EligibilityStatus(String label) {
        this.label = label;
    }

    /** Human-readable label for display in the user interface. */
    public String getLabel() {
        return label;
    }
}
