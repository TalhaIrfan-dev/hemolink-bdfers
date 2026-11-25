
package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.enums.EligibilityStatus;
import com.hemolink.bdfers.service.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var donors = adminService.getAllDonors();

        model.addAttribute("totalDonors", donors.size());
        model.addAttribute("pendingDonors",
                donors.stream()
                        .filter(d -> d.getEligibilityStatus()
                                == EligibilityStatus.REQUIRES_REVIEW)
                        .count());

        model.addAttribute("eligibleDonors",
                donors.stream()
                        .filter(d -> d.getEligibilityStatus()
                                == EligibilityStatus.ELIGIBLE)
                        .count());

        model.addAttribute("ineligibleDonors",
                donors.stream()
                        .filter(d -> d.getEligibilityStatus()
                                == EligibilityStatus.INELIGIBLE)
                        .count());

        return "admin/dashboard";
    }

    @GetMapping("/donors")
    public String donors(Model model) {
        model.addAttribute("donors", adminService.getAllDonors());
        model.addAttribute("eligibilityStatuses",
                EligibilityStatus.values());

        return "admin/donors";
    }

    @PostMapping("/donors/{id}/eligibility")
    public String reviewDonor(
            @PathVariable Long id,
            @RequestParam EligibilityStatus decision,
            @RequestParam String reason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.reviewDonor(
                    id, decision, reason, authentication.getName());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Donor eligibility updated successfully.");

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/admin/donors";
    }
}
