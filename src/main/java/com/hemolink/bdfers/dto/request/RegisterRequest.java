package com.hemolink.bdfers.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Full name is required.")
    @Size(max = 100, message = "Full name cannot exceed 100 characters.")
    private String fullName;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Enter a valid email address, for example name@example.com.")
    @Size(max = 150, message = "Email address cannot exceed 150 characters.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 72,
          message = "Password must be between 8 and 72 characters.")
    private String password;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^(03\\d{9}|\\+923\\d{9})$",
             message = "Enter a Pakistani mobile number as 03XXXXXXXXX or +923XXXXXXXXX, without spaces or dashes.")
    private String phone;

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setPhone(String phone) { this.phone = phone; }
}
