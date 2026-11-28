package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.enums.AvailabilityStatus;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.service.DonorAdminService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin Donor Directory (D-05) and deactivation/reactivation (FR-17).
 * Protected by the /admin/** rule in SecurityConfig.
 */
@Controller
@RequestMapping("/admin/donors")
public class DonorAdminController {

    private final DonorAdminService donorAdminService;

    public DonorAdminController(DonorAdminService donorAdminService) {
        this.donorAdminService = donorAdminService;
    }

    /**
     * Directory with search and filters. {@code status} is ACTIVE,
     * DEACTIVATED or empty/ALL for every donor.
     */
    @GetMapping
    public String directory(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "bloodGroup", required = false) BloodGroup bloodGroup,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "availability", required = false) AvailabilityStatus availability,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        Boolean active = parseActive(status);

        model.addAttribute("donors",
                donorAdminService.search(query, bloodGroup, city, availability, active, page));

        model.addAttribute("q", query == null ? "" : query.trim());
        model.addAttribute("bloodGroupFilter", bloodGroup);
        model.addAttribute("cityFilter", city == null ? "" : city.trim());
        model.addAttribute("availabilityFilter", availability);
        model.addAttribute("statusFilter",
                active == null ? "ALL" : (active ? "ACTIVE" : "DEACTIVATED"));

        model.addAttribute("bloodGroups", BloodGroup.values());
        model.addAttribute("availabilityOptions", AvailabilityStatus.values());
        model.addAttribute("cities", donorAdminService.getDonorCities());

        return "admin/donors";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("detail", donorAdminService.getDetail(id));
        return "admin/donor-detail";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            donorAdminService.deactivate(id, reason, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Donor deactivated. They are excluded from matching, cannot respond, "
                    + "and any shared contact details are hidden.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/donors/" + id;
    }

    @PostMapping("/{id}/reactivate")
    public String reactivate(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            donorAdminService.reactivate(id, reason, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Donor reactivated. They can be matched and respond again.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/donors/" + id;
    }

    private static Boolean parseActive(String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return Boolean.TRUE;
        }
        if ("DEACTIVATED".equalsIgnoreCase(status)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
