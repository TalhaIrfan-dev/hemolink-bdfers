package com.hemolink.bdfers.service;

import com.hemolink.bdfers.enums.GuidanceKey;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Immutable snapshot of the donation guidance settings. A missing (null)
 * value means that check is switched off.
 */
public final class GuidanceSettings {

    private final Map<GuidanceKey, BigDecimal> values;

    public GuidanceSettings(Map<GuidanceKey, BigDecimal> values) {
        EnumMap<GuidanceKey, BigDecimal> copy = new EnumMap<>(GuidanceKey.class);
        if (values != null) {
            values.forEach((key, value) -> {
                if (value != null) {
                    copy.put(key, value);
                }
            });
        }
        this.values = Collections.unmodifiableMap(copy);
    }

    /** Value for a key, or null if the check is switched off. */
    public BigDecimal get(GuidanceKey key) {
        return values.get(key);
    }

    public boolean isOn(GuidanceKey key) {
        return values.containsKey(key);
    }
}
