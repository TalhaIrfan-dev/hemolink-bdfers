package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.enums.BloodGroup;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Anonymous donor availability (decision D-05): counts of active,
 * available donors by blood group and city. Contains no identities.
 */
public class DonorAvailabilityView {

    /** One city row: count per blood group plus totals. */
    public static class CityRow {

        private final String city;
        private final Map<BloodGroup, Long> counts;
        private final long compatibleCount;

        public CityRow(String city, Map<BloodGroup, Long> counts, long compatibleCount) {
            this.city = city;
            EnumMap<BloodGroup, Long> copy = new EnumMap<>(BloodGroup.class);
            copy.putAll(counts);
            this.counts = Collections.unmodifiableMap(copy);
            this.compatibleCount = compatibleCount;
        }

        public String getCity() { return city; }

        public long count(BloodGroup group) {
            return counts.getOrDefault(group, 0L);
        }

        public long getTotal() {
            return counts.values().stream().mapToLong(Long::longValue).sum();
        }

        /** Donors compatible with the selected blood group (0 if none selected). */
        public long getCompatibleCount() { return compatibleCount; }
    }

    private final List<BloodGroup> bloodGroups;
    private final List<CityRow> rows;
    private final BloodGroup neededGroup;
    private final List<BloodGroup> compatibleGroups;

    public DonorAvailabilityView(List<BloodGroup> bloodGroups, List<CityRow> rows,
                                 BloodGroup neededGroup, List<BloodGroup> compatibleGroups) {
        this.bloodGroups = List.copyOf(bloodGroups);
        this.rows = List.copyOf(rows);
        this.neededGroup = neededGroup;
        this.compatibleGroups = List.copyOf(compatibleGroups);
    }

    public List<BloodGroup> getBloodGroups() { return bloodGroups; }
    public List<CityRow> getRows() { return rows; }

    /** Blood group the requester selected, or null. */
    public BloodGroup getNeededGroup() { return neededGroup; }

    /** Donor groups compatible with the selected group (empty if none selected). */
    public List<BloodGroup> getCompatibleGroups() { return compatibleGroups; }

    public long getGrandTotal() {
        return rows.stream().mapToLong(CityRow::getTotal).sum();
    }

    public long totalFor(BloodGroup group) {
        return rows.stream().mapToLong(row -> row.count(group)).sum();
    }
}
