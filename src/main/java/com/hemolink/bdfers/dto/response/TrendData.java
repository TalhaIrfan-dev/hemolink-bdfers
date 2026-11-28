package com.hemolink.bdfers.dto.response;

import java.util.List;

/**
 * Data for the "Blood Request Trend" chart: requests submitted and donor
 * matches created per period bucket. Returned as JSON to Chart.js.
 */
public class TrendData {

    private final String period;
    private final int year;
    private final List<String> labels;
    private final List<Long> requests;
    private final List<Long> matches;

    public TrendData(String period, int year, List<String> labels,
                     List<Long> requests, List<Long> matches) {
        this.period = period;
        this.year = year;
        this.labels = List.copyOf(labels);
        this.requests = List.copyOf(requests);
        this.matches = List.copyOf(matches);
    }

    public String getPeriod() { return period; }
    public int getYear() { return year; }
    public List<String> getLabels() { return labels; }
    public List<Long> getRequests() { return requests; }
    public List<Long> getMatches() { return matches; }
}
