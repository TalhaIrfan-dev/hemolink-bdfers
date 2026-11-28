package com.hemolink.bdfers.enums;

import java.math.BigDecimal;

/**
 * Donation guidance settings (decisions D-02, D-03). Each value is stored
 * in the donation_guidance_settings table; an empty value switches the
 * check off. The allowed ranges below are data-entry limits for the
 * settings page, not medical thresholds.
 */
public enum GuidanceKey {

    MIN_HAEMOGLOBIN_G_DL(
            "Minimum haemoglobin", "g/dL", "5.0", "20.0", false,
            "Haemoglobin below this level is flagged as not advised."),

    MIN_WEIGHT_KG(
            "Minimum weight", "kg", "30", "200", false,
            "Body weight below this level is flagged as not advised."),

    MIN_AGE_YEARS(
            "Minimum age", "years", "16", "100", true,
            "Donors younger than this are flagged as not advised."),

    MAX_AGE_YEARS(
            "Maximum age", "years", "16", "100", true,
            "Donors older than this are flagged as not advised."),

    MIN_DAYS_BETWEEN_DONATIONS(
            "Minimum days between donations", "days", "1", "730", true,
            "A donation sooner than this after the last one is flagged. Leave empty to leave it to the donor."),

    MAX_UNITS_PER_DONATION(
            "Maximum units per donation", "units", "1", "5", true,
            "Offering more units than this is flagged. Leave empty to leave it to the donor."),

    MAX_CBC_AGE_DAYS(
            "Maximum age of CBC result", "days", "1", "365", true,
            "A CBC result older than this is flagged. Leave empty to switch this check off.");

    private final String label;
    private final String unit;
    private final BigDecimal minAllowed;
    private final BigDecimal maxAllowed;
    private final boolean wholeNumber;
    private final String help;

    GuidanceKey(String label, String unit, String minAllowed,
                String maxAllowed, boolean wholeNumber, String help) {
        this.label = label;
        this.unit = unit;
        this.minAllowed = new BigDecimal(minAllowed);
        this.maxAllowed = new BigDecimal(maxAllowed);
        this.wholeNumber = wholeNumber;
        this.help = help;
    }

    public String getLabel() { return label; }
    public String getUnit() { return unit; }
    public BigDecimal getMinAllowed() { return minAllowed; }
    public BigDecimal getMaxAllowed() { return maxAllowed; }
    public boolean isWholeNumber() { return wholeNumber; }
    public String getHelp() { return help; }
}
