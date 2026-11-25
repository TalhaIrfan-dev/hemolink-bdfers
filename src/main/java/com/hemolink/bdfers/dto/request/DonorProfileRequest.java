
package com.hemolink.bdfers.dto.request;

import com.hemolink.bdfers.enums.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DonorProfileRequest {

    @NotNull(message = "Please select your blood group.")
    private BloodGroup bloodGroup;

    @NotBlank(message = "City is required.")
    @Size(max = 100)
    private String city;

    @NotNull(message = "Date of birth is required.")
    @Past(message = "Date of birth must be in the past.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotNull(message = "Weight is required.")
    @DecimalMin(value = "1.0", message = "Weight must be at least 1 kg.")
    @DecimalMax(value = "500.0", message = "Weight cannot exceed 500 kg.")
    private BigDecimal weight;

    @PastOrPresent(message = "Last donation date cannot be in the future.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lastDonationDate;

    @NotNull(message = "Please select your availability.")
    private AvailabilityStatus availability = AvailabilityStatus.AVAILABLE;

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public String getCity() {
        return city;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public AvailabilityStatus getAvailability() {
        return availability;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public void setAvailability(AvailabilityStatus availability) {
        this.availability = availability;
    }
}
