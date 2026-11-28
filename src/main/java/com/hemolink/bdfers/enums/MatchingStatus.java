package com.hemolink.bdfers.enums;

public enum MatchingStatus {
    NOT_STARTED("Not started"),
    IN_PROGRESS("In progress"),
    MATCHES_FOUND("Matches found"),
    NO_MATCHES("No matches");

    private final String label;

    MatchingStatus(String label) {
        this.label = label;
    }

    /** Human-readable label for display in the user interface. */
    public String getLabel() {
        return label;
    }
}
