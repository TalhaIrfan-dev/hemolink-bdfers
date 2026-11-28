package com.hemolink.bdfers.enums;

import java.util.Locale;

/** Tabs on the donor's "Matched Requests" page (Module 3.5). */
public enum DonorRequestTab {

    /** Active requests the donor has not answered yet. */
    WAITING("Waiting for reply"),
    /** Active requests the donor has accepted. */
    ACCEPTED("Accepted"),
    /** Declined by the donor, or the request has ended. */
    PAST("Past");

    private final String label;

    DonorRequestTab(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getParam() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The tab a match belongs to. Every match belongs to exactly one tab. */
    public static DonorRequestTab of(DonorResponse response, boolean requestActive) {
        if (!requestActive || response == DonorResponse.DECLINED) {
            return PAST;
        }
        return response == DonorResponse.ACCEPTED ? ACCEPTED : WAITING;
    }

    /** Lenient parsing; null when missing or unknown (the page picks a default). */
    public static DonorRequestTab fromParam(String value) {
        if (value == null) {
            return null;
        }
        for (DonorRequestTab tab : values()) {
            if (tab.getParam().equals(value.trim().toLowerCase(Locale.ROOT))) {
                return tab;
            }
        }
        return null;
    }
}
