package com.hemolink.bdfers.entity;

import com.hemolink.bdfers.enums.DonorStatusAction;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** One deactivation or reactivation of a donor profile (FR-17). */
@Entity
@Table(name = "donor_status_changes")
public class DonorStatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private DonorProfile donor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonorStatusAction action;

    @Column(nullable = false, length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private AppUser changedBy;

    @Column(name = "changed_at", insertable = false, updatable = false)
    private LocalDateTime changedAt;

    public Long getId() { return id; }
    public DonorProfile getDonor() { return donor; }
    public DonorStatusAction getAction() { return action; }
    public String getReason() { return reason; }
    public AppUser getChangedBy() { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }

    public void setDonor(DonorProfile donor) { this.donor = donor; }
    public void setAction(DonorStatusAction action) { this.action = action; }
    public void setReason(String reason) { this.reason = reason; }
    public void setChangedBy(AppUser changedBy) { this.changedBy = changedBy; }
}
