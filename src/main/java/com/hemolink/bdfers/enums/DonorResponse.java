package com.hemolink.bdfers.enums;

public enum DonorResponse {
    PENDING("Awaiting response"),
    ACCEPTED("Accepted"),
    DECLINED("Declined");

    private final String label;

    DonorResponse(String label) {
        this.label = label;
    }

    /** Human-readable label for display in the user interface. */
    public String getLabel() {
        return label;
    }
}
