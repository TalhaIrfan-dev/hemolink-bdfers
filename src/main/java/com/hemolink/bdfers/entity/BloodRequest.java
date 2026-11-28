
package com.hemolink.bdfers.entity;

import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.enums.MatchingStatus;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.enums.RequestUrgency;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private AppUser requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "units_required", nullable = false)
    private Integer unitsRequired;

    @Column(name = "fulfilled_units", nullable = false)
    private Integer fulfilledUnits = 0;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "hospital_name", nullable = false, length = 150)
    private String hospitalName;

    @Column(name = "required_at", nullable = false)
    private LocalDateTime requiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestUrgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_status", nullable = false)
    private RequestAdminStatus adminStatus = RequestAdminStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_status", nullable = false)
    private MatchingStatus matchingStatus = MatchingStatus.NOT_STARTED;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    /** Administrator who approved or rejected the request (FR-16). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private AppUser reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /** Why the request was closed (required for admins, optional for requesters). */
    @Column(name = "close_reason", length = 255)
    private String closeReason;

    /** Requester or administrator who closed the request. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    private AppUser closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public AppUser getRequester() {
        return requester;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public Integer getUnitsRequired() {
        return unitsRequired;
    }

    public Integer getFulfilledUnits() {
        return fulfilledUnits;
    }

    public String getCity() {
        return city;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public LocalDateTime getRequiredAt() {
        return requiredAt;
    }

    public RequestUrgency getUrgency() {
        return urgency;
    }

    public RequestAdminStatus getAdminStatus() {
        return adminStatus;
    }

    public MatchingStatus getMatchingStatus() {
        return matchingStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRequester(AppUser requester) {
        this.requester = requester;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public void setUnitsRequired(Integer unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public void setFulfilledUnits(Integer fulfilledUnits) {
        this.fulfilledUnits = fulfilledUnits;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public void setRequiredAt(LocalDateTime requiredAt) {
        this.requiredAt = requiredAt;
    }

    public void setUrgency(RequestUrgency urgency) {
        this.urgency = urgency;
    }

    public void setAdminStatus(RequestAdminStatus adminStatus) {
        this.adminStatus = adminStatus;
    }

    public void setMatchingStatus(MatchingStatus matchingStatus) {
        this.matchingStatus = matchingStatus;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public AppUser getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(AppUser reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getCloseReason() { return closeReason; }
    public void setCloseReason(String closeReason) { this.closeReason = closeReason; }

    public AppUser getClosedBy() { return closedBy; }
    public void setClosedBy(AppUser closedBy) { this.closedBy = closedBy; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
