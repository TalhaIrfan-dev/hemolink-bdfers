package com.hemolink.bdfers.service;

import com.hemolink.bdfers.enums.BloodGroup;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.hemolink.bdfers.enums.BloodGroup.*;

/**
 * Predefined ABO/Rh red-cell compatibility rules used to identify
 * potential donors (SRS FR-10). Documented in docs/blood-compatibility.md.
 *
 * <p>For each blood group a patient needs, this lists the donor blood
 * groups whose red cells are compatible:</p>
 *
 * <pre>
 *   Recipient  Compatible donors
 *   O-         O-
 *   O+         O-, O+
 *   A-         O-, A-
 *   A+         O-, O+, A-, A+
 *   B-         O-, B-
 *   B+         O-, O+, B-, B+
 *   AB-        O-, A-, B-, AB-
 *   AB+        all groups
 * </pre>
 *
 * <p>These rules identify <em>potential</em> donors only. They are not a
 * clinical cross-match; final compatibility is confirmed by the hospital
 * or blood bank.</p>
 */
public final class BloodCompatibility {

    private static final Map<BloodGroup, Set<BloodGroup>> COMPATIBLE_DONORS;

    static {
        Map<BloodGroup, Set<BloodGroup>> map = new EnumMap<>(BloodGroup.class);

        map.put(O_NEGATIVE,  EnumSet.of(O_NEGATIVE));
        map.put(O_POSITIVE,  EnumSet.of(O_NEGATIVE, O_POSITIVE));
        map.put(A_NEGATIVE,  EnumSet.of(O_NEGATIVE, A_NEGATIVE));
        map.put(A_POSITIVE,  EnumSet.of(O_NEGATIVE, O_POSITIVE, A_NEGATIVE, A_POSITIVE));
        map.put(B_NEGATIVE,  EnumSet.of(O_NEGATIVE, B_NEGATIVE));
        map.put(B_POSITIVE,  EnumSet.of(O_NEGATIVE, O_POSITIVE, B_NEGATIVE, B_POSITIVE));
        map.put(AB_NEGATIVE, EnumSet.of(O_NEGATIVE, A_NEGATIVE, B_NEGATIVE, AB_NEGATIVE));
        map.put(AB_POSITIVE, EnumSet.allOf(BloodGroup.class));

        COMPATIBLE_DONORS = Collections.unmodifiableMap(map);
    }

    private BloodCompatibility() {
    }

    /**
     * Returns the donor blood groups compatible with the blood group
     * a patient needs.
     */
    public static Set<BloodGroup> compatibleDonorGroups(BloodGroup recipient) {
        if (recipient == null) {
            throw new IllegalArgumentException("Recipient blood group is required.");
        }
        return Collections.unmodifiableSet(
                EnumSet.copyOf(COMPATIBLE_DONORS.get(recipient)));
    }

    /** True if a donor of {@code donor} group may give to {@code recipient}. */
    public static boolean canDonate(BloodGroup donor, BloodGroup recipient) {
        if (donor == null || recipient == null) {
            return false;
        }
        return COMPATIBLE_DONORS.get(recipient).contains(donor);
    }
}
