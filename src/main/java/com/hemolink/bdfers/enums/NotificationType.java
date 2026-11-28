package com.hemolink.bdfers.enums;

/** Kind of in-app notification (FR-13). */
public enum NotificationType {
    GENERAL("General"),
    MATCH("New match"),
    RESPONSE("Donor response"),
    CONSENT("Contact shared"),
    REVIEW("Request review"),
    REQUEST_UPDATE("Request update"),
    DONATION("Donation"),
    ACCOUNT("Account"),
    ADMIN_QUEUE("Needs review");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
