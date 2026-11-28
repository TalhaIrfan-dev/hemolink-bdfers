package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.dto.response.AvailabilitySummary;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.service.DonorAvailabilityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Requester "Donor Availability" page (D-05): anonymous counts only.
 * Under /requests/** so it is restricted to regular users (FR-03).
 */
@Controller
@RequestMapping("/requests/donor-availability")
public class DonorAvailabilityController {

    private final DonorAvailabilityService availabilityService;

    public DonorAvailabilityController(DonorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public String availability(
            @RequestParam(name = "bloodGroup", required = false) BloodGroup bloodGroup,
            Model model) {

        model.addAttribute("view", availabilityService.getAvailability(bloodGroup));
        model.addAttribute("bloodGroups", BloodGroup.values());

        return "requester/donor-availability";
    }

    /**
     * JSON counts for the live "Donor availability" box on the Create
     * Request form (Module 3.4). Counts only, no identities (D-05).
     * Example: GET /requests/donor-availability/summary?bloodGroup=A_POSITIVE&city=Sukkur
     */
    @GetMapping("/summary")
    @ResponseBody
    public AvailabilitySummary summary(
            @RequestParam(name = "bloodGroup") BloodGroup bloodGroup,
            @RequestParam(name = "city", required = false) String city) {

        return availabilityService.getSummary(bloodGroup, city);
    }
}
