package com.hemolink.bdfers.dto.response;

/**
 * Counts-only summary for the live box on the Create Request form
 * (Module 3.4, D-05). Contains no donor identities.
 *
 * @param bloodGroup        label of the blood group needed, e.g. "A+"
 * @param city              city as typed, or null when none was given
 * @param compatibleInCity  available donors who can give to this group in
 *                          the city (null when no city was given)
 * @param compatibleAll     available donors who can give to this group in all cities
 */
public record AvailabilitySummary(
        String bloodGroup,
        String city,
        Long compatibleInCity,
        long compatibleAll) {
}
