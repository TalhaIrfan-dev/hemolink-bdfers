package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.enums.GuidanceKey;

import java.time.LocalDateTime;

/** One row on the admin Donation Guidance Settings page. */
public class GuidanceSettingRow {

    private final GuidanceKey key;
    private final String value;
    private final String updatedByName;
    private final LocalDateTime updatedAt;

    public GuidanceSettingRow(GuidanceKey key, String value,
                              String updatedByName, LocalDateTime updatedAt) {
        this.key = key;
        this.value = value;
        this.updatedByName = updatedByName;
        this.updatedAt = updatedAt;
    }

    public GuidanceKey getKey() { return key; }
    public String getName() { return key.name(); }
    public String getLabel() { return key.getLabel(); }
    public String getUnit() { return key.getUnit(); }
    public String getHelp() { return key.getHelp(); }

    /** Current value as text; empty when the check is off. */
    public String getValue() { return value; }
    public boolean isOn() { return value != null && !value.isEmpty(); }
    public String getUpdatedByName() { return updatedByName; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /** Hint for the input's step attribute. */
    public String getStep() { return key.isWholeNumber() ? "1" : "0.1"; }
    public String getMin() { return key.getMinAllowed().toPlainString(); }
    public String getMax() { return key.getMaxAllowed().toPlainString(); }
}
