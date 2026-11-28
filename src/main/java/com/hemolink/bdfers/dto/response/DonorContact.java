package com.hemolink.bdfers.dto.response;

import java.time.LocalDateTime;

/**
 * Donor contact details released to a requester (FR-15, D-16).
 * Only created when the donor has accepted, consented and the request
 * is still active. Contains no date of birth, weight or health data.
 */
public class DonorContact {

    private final String fullName;
    private final String phone;
    private final String bloodGroupLabel;
    private final String city;
    private final LocalDateTime sharedAt;

    public DonorContact(
            String fullName,
            String phone,
            String bloodGroupLabel,
            String city,
            LocalDateTime sharedAt) {

        this.fullName = fullName;
        this.phone = phone;
        this.bloodGroupLabel = bloodGroupLabel;
        this.city = city;
        this.sharedAt = sharedAt;
    }

    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getBloodGroupLabel() { return bloodGroupLabel; }
    public String getCity() { return city; }
    public LocalDateTime getSharedAt() { return sharedAt; }
}
