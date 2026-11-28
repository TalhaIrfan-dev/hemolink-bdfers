package com.hemolink.bdfers.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Health check entered by a donor when accepting a request (D-01).
 * These are data-entry limits only; the advice itself comes from the
 * configurable guidance settings.
 */
public class HealthCheckRequest {

    @NotNull(message = "Enter your haemoglobin result from your CBC report.")
    @DecimalMin(value = "3.0", message = "Haemoglobin must be at least 3.0 g/dL. Check the value on your CBC report.")
    @DecimalMax(value = "25.0", message = "Haemoglobin cannot be more than 25.0 g/dL. Check the value on your CBC report.")
    @Digits(integer = 2, fraction = 1, message = "Enter haemoglobin with at most one decimal place, for example 13.2.")
    private BigDecimal haemoglobinGdl;

    @NotNull(message = "Enter the date of your CBC test.")
    @PastOrPresent(message = "The CBC date cannot be in the future.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cbcDate;

    @NotNull(message = "Enter your current weight.")
    @DecimalMin(value = "30.0", message = "Weight must be at least 30 kg.")
    @DecimalMax(value = "250.0", message = "Weight cannot be more than 250 kg.")
    @Digits(integer = 3, fraction = 2, message = "Enter weight with at most two decimal places.")
    private BigDecimal weightKg;

    @NotNull(message = "Enter how many units you are offering.")
    @Min(value = 1, message = "You must offer at least 1 unit.")
    @Max(value = 10, message = "You cannot offer more than 10 units.")
    private Integer unitsOffered;

    /** Donor confirms they understand the advice when it is "Not advised". */
    private boolean acknowledged;

    public BigDecimal getHaemoglobinGdl() { return haemoglobinGdl; }
    public LocalDate getCbcDate() { return cbcDate; }
    public BigDecimal getWeightKg() { return weightKg; }
    public Integer getUnitsOffered() { return unitsOffered; }
    public boolean isAcknowledged() { return acknowledged; }

    public void setHaemoglobinGdl(BigDecimal haemoglobinGdl) { this.haemoglobinGdl = haemoglobinGdl; }
    public void setCbcDate(LocalDate cbcDate) { this.cbcDate = cbcDate; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public void setUnitsOffered(Integer unitsOffered) { this.unitsOffered = unitsOffered; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
}
