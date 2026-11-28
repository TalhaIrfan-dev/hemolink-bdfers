package com.hemolink.bdfers.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One donation guidance setting (D-03). The key is the name of a
 * {@link com.hemolink.bdfers.enums.GuidanceKey}; a null value means the
 * check is switched off.
 */
@Entity
@Table(name = "donation_guidance_settings")
public class DonationGuidanceSetting {

    @Id
    @Column(name = "setting_key", length = 50)
    private String settingKey;

    @Column(name = "setting_value", precision = 6, scale = 2)
    private BigDecimal settingValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private AppUser updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DonationGuidanceSetting() {
    }

    public DonationGuidanceSetting(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingKey() { return settingKey; }
    public BigDecimal getSettingValue() { return settingValue; }
    public AppUser getUpdatedBy() { return updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setSettingValue(BigDecimal settingValue) { this.settingValue = settingValue; }
    public void setUpdatedBy(AppUser updatedBy) { this.updatedBy = updatedBy; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
