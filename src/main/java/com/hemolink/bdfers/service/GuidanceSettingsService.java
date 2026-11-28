package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.response.GuidanceChangeRow;
import com.hemolink.bdfers.dto.response.GuidanceSettingRow;
import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.DonationGuidanceSetting;
import com.hemolink.bdfers.entity.GuidanceSettingAudit;
import com.hemolink.bdfers.enums.GuidanceKey;
import com.hemolink.bdfers.exception.NotFoundException;
import com.hemolink.bdfers.repository.DonationGuidanceSettingRepository;
import com.hemolink.bdfers.repository.GuidanceSettingAuditRepository;
import com.hemolink.bdfers.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and updates the donation guidance settings (D-02, D-03).
 * Values are stored in the database, edited by administrators, and every
 * change is recorded in guidance_settings_audit.
 */
@Service
public class GuidanceSettingsService {

    private final DonationGuidanceSettingRepository settingRepository;
    private final GuidanceSettingAuditRepository auditRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public GuidanceSettingsService(
            DonationGuidanceSettingRepository settingRepository,
            GuidanceSettingAuditRepository auditRepository,
            UserRepository userRepository,
            Clock clock) {

        this.settingRepository = settingRepository;
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    /** Current settings. Keys with no row or no value are switched off. */
    @Transactional(readOnly = true)
    public GuidanceSettings current() {
        Map<GuidanceKey, BigDecimal> values = new EnumMap<>(GuidanceKey.class);

        for (DonationGuidanceSetting setting : settingRepository.findAll()) {
            GuidanceKey key = toKey(setting.getSettingKey());
            if (key != null) {
                values.put(key, setting.getSettingValue());
            }
        }
        return new GuidanceSettings(values);
    }

    /** Rows for the admin settings page, in a fixed order. */
    @Transactional(readOnly = true)
    public List<GuidanceSettingRow> listForAdmin() {
        Map<String, DonationGuidanceSetting> stored = new LinkedHashMap<>();
        settingRepository.findAll()
                .forEach(setting -> stored.put(setting.getSettingKey(), setting));

        List<GuidanceSettingRow> rows = new ArrayList<>();

        for (GuidanceKey key : GuidanceKey.values()) {
            DonationGuidanceSetting setting = stored.get(key.name());

            BigDecimal value = setting == null ? null : setting.getSettingValue();
            AppUser updatedBy = setting == null ? null : setting.getUpdatedBy();

            rows.add(new GuidanceSettingRow(
                    key,
                    format(value),
                    updatedBy == null ? null : updatedBy.getFullName(),
                    setting == null ? null : setting.getUpdatedAt()));
        }
        return rows;
    }

    /** The 20 most recent changes, newest first. */
    @Transactional(readOnly = true)
    public List<GuidanceChangeRow> recentChanges() {
        return auditRepository.findTop20ByOrderByIdDesc()
                .stream()
                .map(audit -> {
                    GuidanceKey key = toKey(audit.getSettingKey());
                    return new GuidanceChangeRow(
                            key == null ? audit.getSettingKey() : key.getLabel(),
                            formatOrOff(audit.getOldValue()),
                            formatOrOff(audit.getNewValue()),
                            audit.getChangedBy().getFullName(),
                            audit.getChangedAt());
                })
                .toList();
    }

    /**
     * Validates and saves new values. An empty value switches a check off.
     *
     * @param rawValues text entered for each key
     * @return field errors by key; empty if everything was saved
     */
    @Transactional
    public Map<GuidanceKey, String> update(
            Map<GuidanceKey, String> rawValues, String adminEmail) {

        Map<GuidanceKey, String> errors = new EnumMap<>(GuidanceKey.class);
        Map<GuidanceKey, BigDecimal> parsed = new EnumMap<>(GuidanceKey.class);

        for (GuidanceKey key : GuidanceKey.values()) {
            String raw = rawValues.get(key);
            String text = raw == null ? "" : raw.trim();

            if (text.isEmpty()) {
                parsed.put(key, null);
                continue;
            }

            BigDecimal value;
            try {
                value = new BigDecimal(text);
            } catch (NumberFormatException ex) {
                errors.put(key, "Enter a number, or leave it empty to switch this check off.");
                continue;
            }

            if (value.compareTo(key.getMinAllowed()) < 0
                    || value.compareTo(key.getMaxAllowed()) > 0) {
                errors.put(key, "Enter a value between "
                        + key.getMinAllowed().toPlainString() + " and "
                        + key.getMaxAllowed().toPlainString() + " " + key.getUnit() + ".");
                continue;
            }

            if (key.isWholeNumber() && value.stripTrailingZeros().scale() > 0) {
                errors.put(key, "Enter a whole number.");
                continue;
            }

            if (value.stripTrailingZeros().scale() > 2) {
                errors.put(key, "Use at most two decimal places.");
                continue;
            }

            parsed.put(key, value);
        }

        BigDecimal minAge = parsed.get(GuidanceKey.MIN_AGE_YEARS);
        BigDecimal maxAge = parsed.get(GuidanceKey.MAX_AGE_YEARS);
        if (minAge != null && maxAge != null && minAge.compareTo(maxAge) > 0) {
            errors.put(GuidanceKey.MAX_AGE_YEARS,
                    "Maximum age must be the same as or higher than the minimum age.");
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        AppUser admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new NotFoundException("Administrator not found."));

        LocalDateTime now = LocalDateTime.now(clock);

        for (GuidanceKey key : GuidanceKey.values()) {
            BigDecimal newValue = parsed.get(key);

            DonationGuidanceSetting setting = settingRepository.findById(key.name())
                    .orElseGet(() -> new DonationGuidanceSetting(key.name()));

            BigDecimal oldValue = setting.getSettingValue();

            if (sameValue(oldValue, newValue)) {
                continue;
            }

            setting.setSettingValue(newValue);
            setting.setUpdatedBy(admin);
            setting.setUpdatedAt(now);

            // Flush first so the setting row exists before the audit row
            // that references it is inserted.
            settingRepository.saveAndFlush(setting);

            GuidanceSettingAudit audit = new GuidanceSettingAudit();
            audit.setSettingKey(key.name());
            audit.setOldValue(oldValue);
            audit.setNewValue(newValue);
            audit.setChangedBy(admin);
            auditRepository.save(audit);
        }

        return errors;
    }

    private static boolean sameValue(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.compareTo(b) == 0;
    }

    private static GuidanceKey toKey(String name) {
        try {
            return GuidanceKey.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String format(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private static String formatOrOff(BigDecimal value) {
        return value == null ? "Off" : value.stripTrailingZeros().toPlainString();
    }
}
