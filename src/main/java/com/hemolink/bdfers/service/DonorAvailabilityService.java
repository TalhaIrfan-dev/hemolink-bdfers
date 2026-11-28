package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.response.AvailabilitySummary;
import com.hemolink.bdfers.dto.response.DonorAvailabilityView;
import com.hemolink.bdfers.enums.AvailabilityStatus;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Anonymous donor availability for requesters (decision D-05). Only counts
 * are returned: no names, phone numbers or other identifying details.
 */
@Service
public class DonorAvailabilityService {

    private final DonorProfileRepository donorProfileRepository;

    public DonorAvailabilityService(DonorProfileRepository donorProfileRepository) {
        this.donorProfileRepository = donorProfileRepository;
    }

    /**
     * @param neededGroup optional blood group the requester needs; when
     *                    given, each city also shows how many available
     *                    donors are compatible with it (FR-10 rules)
     */
    @Transactional(readOnly = true)
    public DonorAvailabilityView getAvailability(BloodGroup neededGroup) {

        Set<BloodGroup> compatible = neededGroup == null
                ? Set.of()
                : BloodCompatibility.compatibleDonorGroups(neededGroup);

        // Merge cities case-insensitively ("Sukkur" and "sukkur" are one city).
        Map<String, String> displayName = new LinkedHashMap<>();
        Map<String, Map<BloodGroup, Long>> countsByCity = new LinkedHashMap<>();

        for (Object[] row : donorProfileRepository
                .countAvailableByBloodGroupAndCity(AvailabilityStatus.AVAILABLE)) {

            BloodGroup group = (BloodGroup) row[0];
            String city = ((String) row[1]).trim();
            long count = ((Number) row[2]).longValue();

            String key = city.toLowerCase();
            displayName.putIfAbsent(key, city);
            countsByCity
                    .computeIfAbsent(key, k -> new EnumMap<>(BloodGroup.class))
                    .merge(group, count, Long::sum);
        }

        List<DonorAvailabilityView.CityRow> rows = new ArrayList<>();

        countsByCity.forEach((key, counts) -> {
            long compatibleCount = counts.entrySet().stream()
                    .filter(e -> compatible.contains(e.getKey()))
                    .mapToLong(Map.Entry::getValue)
                    .sum();

            rows.add(new DonorAvailabilityView.CityRow(
                    displayName.get(key), counts, compatibleCount));
        });

        rows.sort(Comparator.comparing(
                DonorAvailabilityView.CityRow::getCity, String.CASE_INSENSITIVE_ORDER));

        return new DonorAvailabilityView(
                List.of(BloodGroup.values()),
                rows,
                neededGroup,
                compatible.stream().sorted().toList());
    }

    /**
     * Counts for the live box on the Create Request form (Module 3.4):
     * available donors who can give to {@code neededGroup}, in the given
     * city (case-insensitive, trimmed) and in all cities. Counts only.
     */
    @Transactional(readOnly = true)
    public AvailabilitySummary getSummary(BloodGroup neededGroup, String city) {

        Set<BloodGroup> compatible = BloodCompatibility.compatibleDonorGroups(neededGroup);
        String cityKey = city == null || city.isBlank()
                ? null
                : city.trim().toLowerCase(Locale.ROOT);

        long inCity = 0;
        long all = 0;

        for (Object[] row : donorProfileRepository
                .countAvailableByBloodGroupAndCity(AvailabilityStatus.AVAILABLE)) {

            if (!compatible.contains((BloodGroup) row[0])) {
                continue;
            }
            long count = ((Number) row[2]).longValue();
            all += count;

            if (cityKey != null
                    && ((String) row[1]).trim().toLowerCase(Locale.ROOT).equals(cityKey)) {
                inCity += count;
            }
        }

        return new AvailabilitySummary(
                neededGroup.getLabel(),
                cityKey == null ? null : city.trim(),
                cityKey == null ? null : inCity,
                all);
    }
}
