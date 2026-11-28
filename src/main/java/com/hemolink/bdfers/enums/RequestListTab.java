package com.hemolink.bdfers.enums;

import java.util.Locale;

/** Status tabs on the requester's "My Requests" page (Module 3.4). */
public enum RequestListTab {

    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    CLOSED("Closed & rejected");

    private final String label;

    RequestListTab(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Lower-case value used in the URL, e.g. ?tab=active. */
    public String getParam() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean includes(RequestAdminStatus status) {
        return switch (this) {
            case ALL -> true;
            case ACTIVE -> status == RequestAdminStatus.PENDING || status == RequestAdminStatus.APPROVED;
            case COMPLETED -> status == RequestAdminStatus.FULFILLED;
            case CLOSED -> status == RequestAdminStatus.CLOSED || status == RequestAdminStatus.REJECTED;
        };
    }

    /** Lenient parsing: an unknown or missing value shows All instead of an error. */
    public static RequestListTab fromParam(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        for (RequestListTab tab : values()) {
            if (tab.getParam().equals(value.trim().toLowerCase(Locale.ROOT))) {
                return tab;
            }
        }
        return ALL;
    }
}
