
package com.hemolink.bdfers.entity;

import com.hemolink.bdfers.enums.DonationOutcome;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_records")
public class DonationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private DonorMatch match;

    @Column(name = "units_donated", nullable = false)
    private Integer unitsDonated = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationOutcome outcome;

    @Column(name = "donation_date")
    private LocalDate donationDate;

    @Column(name = "reported_at", insertable = false, updatable = false)
    private LocalDateTime reportedAt;

    public Long getId() {
        return id;
    }

    public DonorMatch getMatch() {
        return match;
    }

    public void setMatch(DonorMatch match) {
        this.match = match;
    }

    public Integer getUnitsDonated() {
        return unitsDonated;
    }

    public void setUnitsDonated(Integer unitsDonated) {
        this.unitsDonated = unitsDonated;
    }

    public DonationOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(DonationOutcome outcome) {
        this.outcome = outcome;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }
}
