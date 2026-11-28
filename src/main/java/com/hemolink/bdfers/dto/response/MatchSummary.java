package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.enums.DonorResponse;

/**
 * Masked view of one potential donor, shown to the requester (FR-12).
 *
 * Deliberately contains NO donor name, phone, email, date of birth,
 * weight or internal IDs (NFR-03), so a template cannot expose them.
 */
public class MatchSummary {

    private final int number;
    private final String bloodGroupLabel;
    private final String city;
    private final DonorResponse response;
    private final boolean contactShared;
    private final boolean donorUnavailable;

    public MatchSummary(
            int number,
            String bloodGroupLabel,
            String city,
            DonorResponse response,
            boolean contactShared,
            boolean donorUnavailable) {

        this.number = number;
        this.bloodGroupLabel = bloodGroupLabel;
        this.city = city;
        this.response = response;
        this.contactShared = contactShared;
        this.donorUnavailable = donorUnavailable;
    }

    /** Display number, e.g. "Potential donor 3". */
    public int getNumber() { return number; }
    public String getBloodGroupLabel() { return bloodGroupLabel; }
    public String getCity() { return city; }
    public DonorResponse getResponse() { return response; }
    public String getResponseLabel() {
        return donorUnavailable ? "No longer available" : response.getLabel();
    }

    /** True if the donor profile was deactivated by an administrator. */
    public boolean isDonorUnavailable() { return donorUnavailable; }

    /** True when the donor accepted and consented while the request is active. */
    public boolean isContactShared() { return contactShared; }
}
