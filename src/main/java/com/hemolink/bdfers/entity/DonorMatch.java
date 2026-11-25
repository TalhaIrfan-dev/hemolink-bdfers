
package com.hemolink.bdfers.entity;

import com.hemolink.bdfers.enums.AdminApprovalStatus;
import com.hemolink.bdfers.enums.DonorResponse;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "donor_matches",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"request_id", "donor_id"}))
public class DonorMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private BloodRequest bloodRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private DonorProfile donor;

    @Enumerated(EnumType.STRING)
    @Column(name = "donor_response", nullable = false)
    private DonorResponse donorResponse = DonorResponse.PENDING;

    @Column(name = "donor_consent", nullable = false)
    private boolean donorConsent = false;

    @Column(name = "consent_at")
    private LocalDateTime consentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_approval", nullable = false)
    private AdminApprovalStatus adminApproval = AdminApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private AppUser approvedBy;

    @Column(name = "approval_at")
    private LocalDateTime approvalAt;

    public Long getId() {
        return id;
    }

    public BloodRequest getBloodRequest() {
        return bloodRequest;
    }

    public void setBloodRequest(BloodRequest bloodRequest) {
        this.bloodRequest = bloodRequest;
    }

    public DonorProfile getDonor() {
        return donor;
    }

    public void setDonor(DonorProfile donor) {
        this.donor = donor;
    }

    public DonorResponse getDonorResponse() {
        return donorResponse;
    }

    public void setDonorResponse(DonorResponse donorResponse) {
        this.donorResponse = donorResponse;
    }

    public boolean isDonorConsent() {
        return donorConsent;
    }

    public void setDonorConsent(boolean donorConsent) {
        this.donorConsent = donorConsent;
    }

    public LocalDateTime getConsentAt() {
        return consentAt;
    }

    public void setConsentAt(LocalDateTime consentAt) {
        this.consentAt = consentAt;
    }

    public AdminApprovalStatus getAdminApproval() {
        return adminApproval;
    }

    public void setAdminApproval(AdminApprovalStatus adminApproval) {
        this.adminApproval = adminApproval;
    }

    public AppUser getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(AppUser approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovalAt() {
        return approvalAt;
    }

    public void setApprovalAt(LocalDateTime approvalAt) {
        this.approvalAt = approvalAt;
    }
}
