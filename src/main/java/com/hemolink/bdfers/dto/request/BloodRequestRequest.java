
package com.hemolink.bdfers.dto.request;

import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.enums.RequestUrgency;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class BloodRequestRequest {

    @NotNull(message = "Please select a blood group")
    private BloodGroup bloodGroup;

    @NotNull(message = "Units required is mandatory")
    @Min(value = 1, message = "At least 1 unit is required")
    private Integer unitsRequired;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "Hospital name is required")
    @Size(max = 150, message = "Hospital name cannot exceed 150 characters")
    private String hospitalName;

    @NotNull(message = "Required date and time are mandatory")
    @FutureOrPresent(message = "Required date and time cannot be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime requiredAt;

    @NotNull(message = "Please select request urgency")
    private RequestUrgency urgency;

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Integer getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(Integer unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public LocalDateTime getRequiredAt() {
        return requiredAt;
    }

    public void setRequiredAt(LocalDateTime requiredAt) {
        this.requiredAt = requiredAt;
    }

    public RequestUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RequestUrgency urgency) {
        this.urgency = urgency;
    }
}
