package com.hemolink.bdfers.enums;

public enum BloodGroup {
    A_POSITIVE("A+"),
    A_NEGATIVE("A\u2212"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B\u2212"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB\u2212"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O\u2212");

    private final String label;

    BloodGroup(String label) {
        this.label = label;
    }

    /** Human-readable label for display in the user interface. */
    public String getLabel() {
        return label;
    }
}
