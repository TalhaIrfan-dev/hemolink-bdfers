package com.hemolink.bdfers.dto.response;

/** Signed-in user's display details for the top bar (name, initials, role). */
public class CurrentUserView {

    private final String fullName;
    private final String initials;
    private final boolean admin;

    public CurrentUserView(String fullName, boolean admin) {
        this.fullName = fullName;
        this.initials = initialsOf(fullName);
        this.admin = admin;
    }

    public String getFullName() { return fullName; }
    public String getFirstName() {
        String trimmed = fullName == null ? "" : fullName.trim();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed;
    }
    public String getInitials() { return initials; }
    public boolean isAdmin() { return admin; }
    public String getRoleLabel() { return admin ? "Administrator" : "Donor & Requester"; }

    static String initialsOf(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}
