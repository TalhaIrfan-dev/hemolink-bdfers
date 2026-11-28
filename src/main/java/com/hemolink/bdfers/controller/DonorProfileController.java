
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

        model.addAttribute("advice",
                donorProfileService.getProfileAdvice(userDetails.getUsername()));

        model.addAttribute("deactivationNotice",
                donorProfileService.getDeactivationNotice(userDetails.getUsername()));

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

            model.addAttribute("advice",
                    donorProfileService.getProfileAdvice(userDetails.getUsername()));

            model.addAttribute("deactivationNotice",
                    donorProfileService.getDeactivationNotice(userDetails.getUsername()));

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

            model.addAttribute("advice",
                    donorProfileService.getProfileAdvice(userDetails.getUsername()));

            model.addAttribute("deactivationNotice",
                    donorProfileService.getDeactivationNotice(userDetails.getUsername()));

            addFormOptions(model);

            return "donor/profile";
        }
    }

    /** Quick Available/Unavailable switch on the dashboard (FR-05). */
    @PostMapping("/availability")
    public String setAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam AvailabilityStatus availability,
            RedirectAttributes redirectAttributes) {

        try {
            donorProfileService.setAvailability(userDetails.getUsername(), availability);
            redirectAttributes.addFlashAttribute("successMessage",
                    availability == AvailabilityStatus.AVAILABLE
                            ? "You are now available. You will be matched to compatible requests."
                            : "You are now unavailable. You will not be matched to new requests.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/dashboard";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("bloodGroups", BloodGroup.values());
        model.addAttribute("availabilityOptions", AvailabilityStatus.values());
    }
}
