package com.hemolink.bdfers.dto.response;

import java.time.LocalDateTime;

/** One entry in the guidance settings change history. */
public class GuidanceChangeRow {

    private final String settingLabel;
    private final String oldValue;
    private final String newValue;
    private final String changedBy;
    private final LocalDateTime changedAt;

    public GuidanceChangeRow(String settingLabel, String oldValue, String newValue,
                             String changedBy, LocalDateTime changedAt) {
        this.settingLabel = settingLabel;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public String getSettingLabel() { return settingLabel; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getChangedBy() { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
