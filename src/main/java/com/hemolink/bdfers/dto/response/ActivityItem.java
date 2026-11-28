package com.hemolink.bdfers.dto.response;

import java.time.LocalDateTime;

/** One entry in an activity feed (admin recent activity). */
public class ActivityItem {

    private final String kind;
    private final String title;
    private final String detail;
    private final LocalDateTime time;
    private final String link;

    public ActivityItem(String kind, String title, String detail, LocalDateTime time, String link) {
        this.kind = kind;
        this.title = title;
        this.detail = detail;
        this.time = time;
        this.link = link;
    }

    /** Short key used for the icon, e.g. REVIEW, CLOSE, DONOR, SETTINGS. */
    public String getKind() { return kind; }
    public String getTitle() { return title; }
    public String getDetail() { return detail; }
    public LocalDateTime getTime() { return time; }
    public String getLink() { return link; }
}
