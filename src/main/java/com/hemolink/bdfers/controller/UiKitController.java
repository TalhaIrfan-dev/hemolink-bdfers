package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.enums.AdminApprovalStatus;
import com.hemolink.bdfers.enums.AdviceLevel;
import com.hemolink.bdfers.enums.AvailabilityStatus;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.enums.DonationOutcome;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.MatchingStatus;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.enums.RequestUrgency;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

/**
 * UI Kit (Module 3.1): every design-system component on one page, for
 * review and demonstration. Administrators only (/admin/**). The page
 * uses clearly labelled sample values; it does not show user data.
 */
@Controller
public class UiKitController {

    @GetMapping("/admin/ui-kit")
    public String uiKit(Model model) {
        model.addAttribute("requestStatuses", RequestAdminStatus.values());
        model.addAttribute("matchingStatuses", MatchingStatus.values());
        model.addAttribute("responses", DonorResponse.values());
        model.addAttribute("availability", AvailabilityStatus.values());
        model.addAttribute("outcomes", DonationOutcome.values());
        model.addAttribute("advice", AdviceLevel.values());
        model.addAttribute("legacyApproval", AdminApprovalStatus.values());
        model.addAttribute("urgencies", RequestUrgency.values());
        model.addAttribute("bloodGroups", BloodGroup.values());
        model.addAttribute("currentYear", LocalDate.now().getYear());
        model.addAttribute("iconNames", List.of(
                "home", "plus-square", "list", "users", "user", "bell", "settings",
                "logout", "search", "drop", "check-circle", "hourglass", "alert",
                "bolt", "calendar", "clock", "activity", "chart", "heart", "shield",
                "eye", "clipboard", "map-pin", "filter", "lock", "mail", "phone",
                "info", "history"));

        // Sample page object to demonstrate the pager (page 2 of 3).
        model.addAttribute("samplePage",
                new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 6));

        return "admin/ui-kit";
    }
}
