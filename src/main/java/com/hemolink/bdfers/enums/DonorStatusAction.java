package com.hemolink.bdfers.enums;

/** Administrative change to a donor profile's active state (FR-17). */
public enum DonorStatusAction {
    DEACTIVATED("Deactivated"),
    REACTIVATED("Reactivated");

    private final String label;

    DonorStatusAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
