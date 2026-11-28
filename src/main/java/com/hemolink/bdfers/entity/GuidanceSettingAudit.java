package com.hemolink.bdfers.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Change history for donation guidance settings (D-03). */
@Entity
@Table(name = "guidance_settings_audit")
public class GuidanceSettingAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, length = 50)
    private String settingKey;

    @Column(name = "old_value", precision = 6, scale = 2)
    private BigDecimal oldValue;

    @Column(name = "new_value", precision = 6, scale = 2)
    private BigDecimal newValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private AppUser changedBy;

    @Column(name = "changed_at", insertable = false, updatable = false)
    private LocalDateTime changedAt;

    public Long getId() { return id; }
    public String getSettingKey() { return settingKey; }
    public BigDecimal getOldValue() { return oldValue; }
    public BigDecimal getNewValue() { return newValue; }
    public AppUser getChangedBy() { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }

    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public void setOldValue(BigDecimal oldValue) { this.oldValue = oldValue; }
    public void setNewValue(BigDecimal newValue) { this.newValue = newValue; }
    public void setChangedBy(AppUser changedBy) { this.changedBy = changedBy; }
}
