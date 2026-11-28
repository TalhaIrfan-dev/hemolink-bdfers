package com.hemolink.bdfers.enums;

/** Result of the advisory eligibility check (D-01). Never blocks a donor. */
public enum AdviceLevel {
    OK("Looks OK"),
    NOT_ADVISED("Not advised");

    private final String label;

    AdviceLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
