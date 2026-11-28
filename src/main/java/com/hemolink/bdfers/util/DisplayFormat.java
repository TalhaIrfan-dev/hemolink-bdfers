package com.hemolink.bdfers.util;

import com.hemolink.bdfers.dto.response.LabelCount;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.enums.NotificationType;
import com.hemolink.bdfers.enums.RequestStage;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Formatting helpers for templates. Available in Thymeleaf as
 * {@code @display}, for example:
 *
 * <pre>
 *   th:text="${@display.requestId(request.id)}"        -> REQ-0012
 *   th:text="${@display.dateTime(request.requiredAt)}" -> 28 Nov 2026, 14:30
 *   th:text="${@display.stageLabel(request)}"          -> Awaiting approval
 *   th:text="${@display.relative(n.createdAt)}"        -> 10 minutes ago
 * </pre>
 *
 * Thymeleaf 3.1 does not allow static Java calls in templates, so
 * formatting lives here.
 */
@Component("display")
public class DisplayFormat {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);

    /** Same order as the donut colours in static/js/charts.js. */
    private static final List<String> CHART_COLORS = List.of(
            "#0b5d4f", "#12ad7b", "#4fd1a5", "#9be7c8",
            "#087566", "#d97706", "#64748b", "#c81e35");

    private final Clock clock;

    public DisplayFormat(Clock clock) {
        this.clock = clock;
    }

    // ---------------- IDs and dates ----------------

    /** Formats a blood request ID as REQ-0001. */
    public String requestId(Long id) {
        return id == null ? "-" : String.format("REQ-%04d", id);
    }

    public String date(LocalDate date) {
        return date == null ? "-" : date.format(DATE);
    }

    public String dateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME);
    }

    /** Age in whole years today, or "-" if the date of birth is unknown. */
    public String age(LocalDate dateOfBirth) {
        return dateOfBirth == null
                ? "-"
                : String.valueOf(Period.between(dateOfBirth, LocalDate.now(clock)).getYears());
    }

    /** "Just now", "10 minutes ago", "3 hours ago", "Yesterday", "4 days ago", or a date. */
    public String relative(LocalDateTime time) {
        if (time == null) {
            return "-";
        }
        Duration ago = Duration.between(time, LocalDateTime.now(clock));
        long minutes = ago.toMinutes();

        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        }
        long hours = ago.toHours();
        if (hours < 24) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        }
        long days = ago.toDays();
        if (days == 1) {
            return "Yesterday";
        }
        if (days < 7) {
            return days + " days ago";
        }
        return date(time.toLocalDate());
    }

    /** "Good morning", "Good afternoon" or "Good evening" (server clock). */
    public String greeting() {
        int hour = LocalDateTime.now(clock).getHour();
        if (hour < 12) {
            return "Good morning";
        }
        if (hour < 17) {
            return "Good afternoon";
        }
        return "Good evening";
    }

    public int currentYear() {
        return LocalDate.now(clock).getYear();
    }

    // ---------------- Combined request stage (Module 3.3, approved mapping) ----------------

    /**
     * One status a requester understands, combining admin status, urgency
     * and matching status:
     * Normal + Pending → Awaiting approval; active with matches → Matching;
     * active without matches → No donors yet; otherwise the admin status.
     */
    public String stageLabel(BloodRequest request) {
        return stage(request).getLabel();
    }

    /** CSS pill modifier for {@link #stageLabel(BloodRequest)}. */
    public String stageClass(BloodRequest request) {
        return stageClass(stage(request));
    }

    /** Label of an already computed stage (request detail and matching pages). */
    public String stageLabel(RequestStage stage) {
        return stage == null ? "-" : stage.getLabel();
    }

    /** CSS pill modifier for a stage. */
    public String stageClass(RequestStage stage) {
        if (stage == null) {
            return "";
        }
        return switch (stage) {
            case AWAITING_APPROVAL, STARTING -> "hl-pill--pending";
            case MATCHING -> "hl-pill--matches_found";
            case NO_DONORS -> "hl-pill--no_matches";
            default -> "hl-pill--" + stage.name().toLowerCase(Locale.ROOT);
        };
    }

    private static RequestStage stage(BloodRequest request) {
        return RequestStage.of(request.getAdminStatus(), request.getUrgency(),
                request.getMatchingStatus());
    }

    // ---------------- Notifications and charts ----------------

    /** Icon name for a notification type (see fragments/icons). */
    public String notificationIcon(NotificationType type) {
        if (type == null) {
            return "bell";
        }
        return switch (type) {
            case MATCH -> "users";
            case RESPONSE -> "check";
            case CONSENT -> "shield";
            case REVIEW, ADMIN_QUEUE -> "clipboard";
            case DONATION -> "drop";
            case ACCOUNT -> "user";
            case REQUEST_UPDATE -> "info";
            default -> "bell";
        };
    }

    /** Timeline style for a notification type: "", "is-warning" or "is-danger". */
    public String notificationTone(NotificationType type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case REVIEW, ADMIN_QUEUE -> "is-warning";
            case ACCOUNT -> "is-danger";
            default -> "";
        };
    }

    /** Colour for the i-th donut segment (matches charts.js). */
    public String chartColor(int index) {
        return CHART_COLORS.get(Math.floorMod(index, CHART_COLORS.size()));
    }

    /** Whole-number percentage, e.g. 7 of 24 → "29%". */
    public String percent(long part, long total) {
        return total <= 0 ? "0%" : Math.round(part * 100.0 / total) + "%";
    }

    /** JSON array of labels, for data-donut-labels. */
    public String labelsJson(List<LabelCount> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append('"').append(escapeJson(items.get(i).getLabel())).append('"');
        }
        return json.append(']').toString();
    }

    /** JSON array of counts, for data-donut-values. */
    public String countsJson(List<LabelCount> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(items.get(i).getCount());
        }
        return json.append(']').toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
