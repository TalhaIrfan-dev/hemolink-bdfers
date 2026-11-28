package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.response.Advice;
import com.hemolink.bdfers.enums.AdviceLevel;
import com.hemolink.bdfers.enums.GuidanceKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Advisory eligibility checks (FR-06 as changed by D-01).
 *
 * <p>The result is guidance only. It never blocks matching or acceptance;
 * the donor makes the final decision. It is not a medical assessment.</p>
 *
 * <p>All dates are calculated from the injected {@link Clock}, so tests
 * can use a fixed reference date (NFR-07).</p>
 */
@Component
public class EligibilityAdvisor {

    private final Clock clock;

    public EligibilityAdvisor(Clock clock) {
        this.clock = clock;
    }

    /** Input values for an assessment. Null values are simply not checked. */
    public record Input(
            LocalDate dateOfBirth,
            LocalDate lastDonationDate,
            BigDecimal weightKg,
            BigDecimal haemoglobinGdl,
            LocalDate cbcDate,
            Integer unitsOffered) {
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public Advice assess(Input input, GuidanceSettings settings) {

        LocalDate today = today();
        List<String> reasons = new ArrayList<>();
        List<String> facts = new ArrayList<>();

        // Age
        if (input.dateOfBirth() != null) {
            int age = Period.between(input.dateOfBirth(), today).getYears();
            BigDecimal minAge = settings.get(GuidanceKey.MIN_AGE_YEARS);
            BigDecimal maxAge = settings.get(GuidanceKey.MAX_AGE_YEARS);

            if (minAge != null && age < minAge.intValue()) {
                reasons.add("Age " + age + " is below the guidance minimum of "
                        + fmt(minAge) + " years.");
            }
            if (maxAge != null && age > maxAge.intValue()) {
                reasons.add("Age " + age + " is above the guidance maximum of "
                        + fmt(maxAge) + " years.");
            }
        }

        // Weight
        BigDecimal minWeight = settings.get(GuidanceKey.MIN_WEIGHT_KG);
        if (input.weightKg() != null && minWeight != null
                && input.weightKg().compareTo(minWeight) < 0) {
            reasons.add("Weight " + fmt(input.weightKg())
                    + " kg is below the guidance minimum of " + fmt(minWeight) + " kg.");
        }

        // Haemoglobin
        BigDecimal minHb = settings.get(GuidanceKey.MIN_HAEMOGLOBIN_G_DL);
        if (input.haemoglobinGdl() != null && minHb != null
                && input.haemoglobinGdl().compareTo(minHb) < 0) {
            reasons.add("Haemoglobin " + fmt(input.haemoglobinGdl())
                    + " g/dL is below the guidance level of " + fmt(minHb) + " g/dL.");
        }

        // CBC age
        if (input.cbcDate() != null) {
            long cbcDays = ChronoUnit.DAYS.between(input.cbcDate(), today);
            facts.add("Your CBC result is " + days(cbcDays) + " old.");

            BigDecimal maxCbcAge = settings.get(GuidanceKey.MAX_CBC_AGE_DAYS);
            if (maxCbcAge != null && cbcDays > maxCbcAge.longValue()) {
                reasons.add("The CBC result is older than the guidance limit of "
                        + fmt(maxCbcAge) + " days. A recent test is recommended.");
            }
        }

        // Time since last donation
        if (input.lastDonationDate() != null) {
            long sinceLast = ChronoUnit.DAYS.between(input.lastDonationDate(), today);
            facts.add("Your last donation was " + days(sinceLast) + " ago.");

            BigDecimal minInterval = settings.get(GuidanceKey.MIN_DAYS_BETWEEN_DONATIONS);
            if (minInterval != null && sinceLast < minInterval.longValue()) {
                reasons.add("Your last donation was " + days(sinceLast)
                        + " ago, which is sooner than the guidance interval of "
                        + fmt(minInterval) + " days.");
            }
        } else {
            facts.add("No previous donation is recorded.");
        }

        // Units offered
        if (input.unitsOffered() != null) {
            facts.add("You are offering " + input.unitsOffered()
                    + (input.unitsOffered() == 1 ? " unit." : " units."));

            BigDecimal maxUnits = settings.get(GuidanceKey.MAX_UNITS_PER_DONATION);
            if (maxUnits != null && input.unitsOffered() > maxUnits.intValue()) {
                reasons.add("Offering " + input.unitsOffered()
                        + " units is more than the guidance maximum of "
                        + fmt(maxUnits) + " per donation.");
            }
        }

        AdviceLevel level = reasons.isEmpty() ? AdviceLevel.OK : AdviceLevel.NOT_ADVISED;
        return new Advice(level, reasons, facts);
    }

    private static String fmt(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private static String days(long days) {
        return days == 1 ? "1 day" : days + " days";
    }
}
