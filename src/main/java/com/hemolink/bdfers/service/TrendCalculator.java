package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.response.TrendData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Splits a time range into chart buckets and counts timestamps per bucket.
 * Pure Java (no database), so it can be unit-tested with fixed dates.
 *
 * <ul>
 *   <li>WEEK  - the last 7 days including today, one bucket per day;</li>
 *   <li>MONTH - the last 30 days including today, one bucket per day;</li>
 *   <li>YEAR  - January to December of the chosen year, one bucket per month.</li>
 * </ul>
 */
public final class TrendCalculator {

    public enum Period { WEEK, MONTH, YEAR }

    private static final DateTimeFormatter DAY_LABEL =
            DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    /** Inclusive start and exclusive end of every bucket, plus its label. */
    public record Bucket(LocalDateTime start, LocalDateTime end, String label) {
    }

    private TrendCalculator() {
    }

    public static Period parsePeriod(String value) {
        if (value == null) {
            return Period.YEAR;
        }
        try {
            return Period.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return Period.YEAR;
        }
    }

    public static List<Bucket> buckets(Period period, int year, LocalDate today) {
        List<Bucket> buckets = new ArrayList<>();

        switch (period) {
            case WEEK, MONTH -> {
                int days = period == Period.WEEK ? 7 : 30;
                LocalDate first = today.minusDays(days - 1L);
                for (int i = 0; i < days; i++) {
                    LocalDate day = first.plusDays(i);
                    buckets.add(new Bucket(
                            day.atStartOfDay(),
                            day.plusDays(1).atStartOfDay(),
                            day.format(DAY_LABEL)));
                }
            }
            case YEAR -> {
                for (int month = 1; month <= 12; month++) {
                    LocalDate first = LocalDate.of(year, month, 1);
                    buckets.add(new Bucket(
                            first.atStartOfDay(),
                            first.plusMonths(1).atStartOfDay(),
                            first.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)));
                }
            }
        }
        return buckets;
    }

    /** Counts how many timestamps fall into each bucket (nulls are ignored). */
    public static List<Long> count(List<Bucket> buckets, List<LocalDateTime> timestamps) {
        long[] counts = new long[buckets.size()];

        for (LocalDateTime time : timestamps) {
            if (time == null) {
                continue;
            }
            for (int i = 0; i < buckets.size(); i++) {
                Bucket bucket = buckets.get(i);
                if (!time.isBefore(bucket.start()) && time.isBefore(bucket.end())) {
                    counts[i]++;
                    break;
                }
            }
        }

        List<Long> result = new ArrayList<>(counts.length);
        for (long c : counts) {
            result.add(c);
        }
        return result;
    }

    public static TrendData build(Period period, int year, List<Bucket> buckets,
                                  List<LocalDateTime> requestTimes,
                                  List<LocalDateTime> matchTimes) {
        return new TrendData(
                period.name(),
                year,
                buckets.stream().map(Bucket::label).toList(),
                count(buckets, requestTimes),
                count(buckets, matchTimes));
    }
}
