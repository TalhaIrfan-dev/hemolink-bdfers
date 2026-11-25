package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.enums.AdminApprovalStatus;
import com.hemolink.bdfers.enums.EligibilityStatus;
import com.hemolink.bdfers.enums.RequestAdminStatus;
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

        model.addAttribute(
                "pendingBloodRequests",
                adminService.getPendingBloodRequests().size());

        return "admin/dashboard";
    }

    // ---------------- DONOR ELIGIBILITY ----------------

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
                    "successMessage",
                    "Donor eligibility updated successfully.");

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/admin/donors";
    }

    // ---------------- BLOOD REQUEST REVIEW ----------------

    @GetMapping("/blood-requests")
    public String bloodRequests(Model model) {

        model.addAttribute(
                "bloodRequests",
                adminService.getPendingBloodRequests());

        model.addAttribute(
                "allBloodRequests",
                adminService.getAllBloodRequests());

        return "admin/blood-requests";
    }

    @PostMapping("/blood-requests/{id}/review")
    public String reviewBloodRequest(
            @PathVariable Long id,
            @RequestParam RequestAdminStatus decision,
            @RequestParam(required = false) String rejectionReason,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.reviewBloodRequest(
                    id, decision, rejectionReason);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    decision == RequestAdminStatus.APPROVED
                            ? "Blood request approved successfully."
                            : "Blood request rejected successfully.");

        } catch (IllegalArgumentException | IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/admin/blood-requests";
    }

    // ---------------- CONTACT DISCLOSURE REVIEW ----------------

    @GetMapping("/disclosures")
    public String disclosures(Model model) {

        model.addAttribute(
                "disclosureRequests",
                adminService.getPendingDisclosureRequests());

        return "admin/disclosures";
    }

    @PostMapping("/disclosures/{matchId}/review")
    public String reviewDisclosure(
            @PathVariable Long matchId,
            @RequestParam AdminApprovalStatus decision,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.reviewContactDisclosure(
                    matchId, decision, authentication.getName());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Contact disclosure decision recorded successfully.");

        } catch (IllegalArgumentException | IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/admin/disclosures";
    }
}