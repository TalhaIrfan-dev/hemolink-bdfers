package com.hemolink.bdfers.dto.request;

import com.hemolink.bdfers.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class DonorProfileRequest {

    @NotNull
    private BloodGroup bloodGroup;

    @NotBlank
    @Size(max = 100)
    private String city;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("500.0")
    private BigDecimal weight;

    @PastOrPresent
    private LocalDate lastDonationDate;

    @NotNull
    private AvailabilityStatus availability = AvailabilityStatus.AVAILABLE;

    public BloodGroup getBloodGroup() { return bloodGroup; }
    public String getCity() { return city; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public BigDecimal getWeight() { return weight; }
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public AvailabilityStatus getAvailability() { return availability; }

    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setCity(String city) { this.city = city; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    public void setAvailability(AvailabilityStatus availability) { this.availability = availability; }
}