package com.hemolink.bdfers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Change password: the current password is required (NFR-02). */
public class PasswordChangeRequest {

    @NotBlank(message = "Enter your current password.")
    private String currentPassword;

    @NotBlank(message = "Enter a new password.")
    @Size(min = 8, max = 72, message = "The new password must be between 8 and 72 characters.")
    private String newPassword;

    @NotBlank(message = "Type the new password again.")
    private String confirmPassword;

    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword() { return newPassword; }
    public String getConfirmPassword() { return confirmPassword; }

    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
