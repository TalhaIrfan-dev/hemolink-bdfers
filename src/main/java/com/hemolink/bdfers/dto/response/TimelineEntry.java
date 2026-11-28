package com.hemolink.bdfers.dto.response;

import java.time.LocalDateTime;

/** One step in a blood request's timeline. */
public class TimelineEntry {

    /** done = completed, current = happening now, pending = not reached. */
    public enum State { DONE, CURRENT, PENDING }

    private final String title;
    private final String detail;
    private final LocalDateTime time;
    private final State state;

    public TimelineEntry(String title, String detail, LocalDateTime time, State state) {
        this.title = title;
        this.detail = detail;
        this.time = time;
        this.state = state;
    }

    public String getTitle() { return title; }
    public String getDetail() { return detail; }
    public LocalDateTime getTime() { return time; }
    public State getState() { return state; }
    public String getStateName() { return state.name().toLowerCase(); }
}
