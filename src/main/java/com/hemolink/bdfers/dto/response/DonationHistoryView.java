package com.hemolink.bdfers.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Donor's donation history page (master prompt: donor donation history). */
public class DonationHistoryView {

    /** One reported donation outcome. */
    public static class Row {
        private final Long requestId;
        private final String bloodGroupLabel;
        private final String hospitalName;
        private final String city;
        private final String outcomeLabel;
        private final boolean completed;
        private final int unitsDonated;
        private final LocalDate donationDate;
        private final LocalDateTime reportedAt;

        public Row(Long requestId, String bloodGroupLabel, String hospitalName, String city,
                   String outcomeLabel, boolean completed, int unitsDonated,
                   LocalDate donationDate, LocalDateTime reportedAt) {
            this.requestId = requestId;
            this.bloodGroupLabel = bloodGroupLabel;
            this.hospitalName = hospitalName;
            this.city = city;
            this.outcomeLabel = outcomeLabel;
            this.completed = completed;
            this.unitsDonated = unitsDonated;
            this.donationDate = donationDate;
            this.reportedAt = reportedAt;
        }

        public Long getRequestId() { return requestId; }
        public String getBloodGroupLabel() { return bloodGroupLabel; }
        public String getHospitalName() { return hospitalName; }
        public String getCity() { return city; }
        public String getOutcomeLabel() { return outcomeLabel; }
        public boolean isCompleted() { return completed; }
        public int getUnitsDonated() { return unitsDonated; }
        public LocalDate getDonationDate() { return donationDate; }
        public LocalDateTime getReportedAt() { return reportedAt; }
    }

    private final List<Row> rows;
    private final long completedDonations;
    private final long totalUnits;
    private final LocalDate lastDonationDate;
    private final LocalDate nextSuggestedDate;
    private final Integer intervalDays;

    public DonationHistoryView(List<Row> rows, long completedDonations, long totalUnits,
                               LocalDate lastDonationDate, LocalDate nextSuggestedDate,
                               Integer intervalDays) {
        this.rows = List.copyOf(rows);
        this.completedDonations = completedDonations;
        this.totalUnits = totalUnits;
        this.lastDonationDate = lastDonationDate;
        this.nextSuggestedDate = nextSuggestedDate;
        this.intervalDays = intervalDays;
    }

    public List<Row> getRows() { return rows; }
    public long getCompletedDonations() { return completedDonations; }
    public long getTotalUnits() { return totalUnits; }

    /** From the donor profile (includes donations recorded before HemoLink). */
    public LocalDate getLastDonationDate() { return lastDonationDate; }

    /** Advisory only; null unless a donation interval is configured (D-01, D-02). */
    public LocalDate getNextSuggestedDate() { return nextSuggestedDate; }
    public Integer getIntervalDays() { return intervalDays; }
}
