package com.hemolink.bdfers.enums;

public enum RequestUrgency {
    CRITICAL("Critical"),
    NORMAL("Normal");

    private final String label;

    RequestUrgency(String label) {
        this.label = label;
    }

    /** Human-readable label for display in the user interface. */
    public String getLabel() {
        return label;
    }
}
