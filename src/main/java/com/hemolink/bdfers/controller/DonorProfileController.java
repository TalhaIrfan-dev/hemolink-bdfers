
package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.dto.request.DonorProfileRequest;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.*;
import com.hemolink.bdfers.service.DonorProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/donor")
public class DonorProfileController {

    private final DonorProfileService donorProfileService;

    public DonorProfileController(DonorProfileService donorProfileService) {
        this.donorProfileService = donorProfileService;
    }

    @GetMapping("/profile")
    public String showProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        DonorProfile profile =
                donorProfileService.getProfile(userDetails.getUsername());

        DonorProfileRequest request = new DonorProfileRequest();

        if (profile != null) {
            request.setBloodGroup(profile.getBloodGroup());
            request.setCity(profile.getCity());
            request.setDateOfBirth(profile.getDateOfBirth());
            request.setWeight(profile.getWeight());
            request.setLastDonationDate(profile.getLastDonationDate());
            request.setAvailability(profile.getAvailability());
        }

        model.addAttribute("profileRequest", request);
        model.addAttribute("profileExists", profile != null);

        model.addAttribute("eligibilityStatus",
                profile != null ? profile.getEligibilityStatus() : null);

        model.addAttribute("eligibilityReason",
                profile != null ? profile.getEligibilityReason() : null);

        addFormOptions(model);

        return "donor/profile";
    }

    @PostMapping("/profile")
    public String saveProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("profileRequest") DonorProfileRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            DonorProfile profile =
                    donorProfileService.getProfile(userDetails.getUsername());

            model.addAttribute("profileExists", profile != null);

            model.addAttribute("eligibilityStatus",
                    profile != null ? profile.getEligibilityStatus() : null);

            model.addAttribute("eligibilityReason",
                    profile != null ? profile.getEligibilityReason() : null);

            addFormOptions(model);

            return "donor/profile";
        }

        try {
            donorProfileService.saveProfile(userDetails.getUsername(), request);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Donor profile saved successfully."
            );

            return "redirect:/donor/profile";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("errorMessage", ex.getMessage());

            DonorProfile profile =
                    donorProfileService.getProfile(userDetails.getUsername());

            model.addAttribute("profileExists", profile != null);

            model.addAttribute("eligibilityStatus",
                    profile != null ? profile.getEligibilityStatus() : null);

            model.addAttribute("eligibilityReason",
                    profile != null ? profile.getEligibilityReason() : null);

            addFormOptions(model);

            return "donor/profile";
        }
    }

    private void addFormOptions(Model model) {
        model.addAttribute("bloodGroups", BloodGroup.values());
        model.addAttribute("availabilityOptions", AvailabilityStatus.values());
    }
}
