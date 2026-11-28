package com.hemolink.bdfers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Account settings: name and phone. Email cannot be changed (login ID). */
public class AccountProfileRequest {

    @NotBlank(message = "Full name is required.")
    @Size(max = 100, message = "Full name cannot exceed 100 characters.")
    private String fullName;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^(03\\d{9}|\\+923\\d{9})$",
             message = "Enter a Pakistani mobile number as 03XXXXXXXXX or +923XXXXXXXXX, without spaces or dashes.")
    private String phone;

    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
}
