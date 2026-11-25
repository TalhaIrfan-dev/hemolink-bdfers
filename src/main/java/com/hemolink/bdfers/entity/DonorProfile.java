package com.hemolink.bdfers.entity;

import com.hemolink.bdfers.enums.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donor_profiles")
public class DonorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availability = AvailabilityStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_status", nullable = false)
    private EligibilityStatus eligibilityStatus =
            EligibilityStatus.REQUIRES_REVIEW;

    @Column(name = "eligibility_reason")
    private String eligibilityReason;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public String getCity() { return city; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public BigDecimal getWeight() { return weight; }
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public AvailabilityStatus getAvailability() { return availability; }
    public EligibilityStatus getEligibilityStatus() { return eligibilityStatus; }
    public String getEligibilityReason() { return eligibilityReason; }
    public boolean isActive() { return active; }

    public void setUser(AppUser user) { this.user = user; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setCity(String city) { this.city = city; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    public void setAvailability(AvailabilityStatus availability) { this.availability = availability; }
    public void setEligibilityStatus(EligibilityStatus eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }
    public void setEligibilityReason(String eligibilityReason) { this.eligibilityReason = eligibilityReason; }
    public void setActive(boolean active) { this.active = active; }
}