package com.hemolink.bdfers.enums;

public enum DonationOutcome {
    COMPLETED("Completed"),
    NOT_OCCURRED("Did not take place");

    private final String label;

    DonationOutcome(String label) {
        this.label = label;
    }

    /** Human-readable label for display in the user interface. */
    public String getLabel() {
        return label;
    }
}
