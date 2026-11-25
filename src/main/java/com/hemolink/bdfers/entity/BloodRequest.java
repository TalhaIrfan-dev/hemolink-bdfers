
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

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

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
}
