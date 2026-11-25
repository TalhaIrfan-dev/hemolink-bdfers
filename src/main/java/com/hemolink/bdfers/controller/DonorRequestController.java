
package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.enums.DonationOutcome;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.service.DonationReportingService;
import com.hemolink.bdfers.service.DonorRequestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/donor/requests")
public class DonorRequestController {

    private final DonorRequestService donorRequestService;
    private final DonationReportingService donationReportingService;

    public DonorRequestController(
            DonorRequestService donorRequestService,
            DonationReportingService donationReportingService) {

        this.donorRequestService = donorRequestService;
        this.donationReportingService = donationReportingService;
    }

    @GetMapping
    public String myRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();

        model.addAttribute(
                "matches",
                donorRequestService.getMyMatchedRequests(email));

        model.addAttribute(
                "reportedMatchIds",
                donationReportingService.getReportedMatchIds(email));

        model.addAttribute("today", LocalDate.now());

        return "donor/requests";
    }

    @PostMapping("/{matchId}/respond")
    public String respond(
            @PathVariable Long matchId,
            @RequestParam DonorResponse response,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            donorRequestService.respondToRequest(
                    matchId,
                    response,
                    userDetails.getUsername());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your response has been recorded.");

        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());
        }

        return "redirect:/donor/requests";
    }

    @PostMapping("/{matchId}/consent")
    public String consent(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            donorRequestService.giveContactConsent(
                    matchId,
                    userDetails.getUsername());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your contact-sharing consent has been recorded. "
                    + "Administrator approval is still required.");

        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());
        }

        return "redirect:/donor/requests";
    }

    @PostMapping("/{matchId}/donation-report")
    public String reportDonationOutcome(
            @PathVariable Long matchId,
            @RequestParam DonationOutcome outcome,
            @RequestParam(required = false) Integer unitsDonated,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate donationDate,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            donationReportingService.reportOutcome(
                    matchId,
                    userDetails.getUsername(),
                    outcome,
                    unitsDonated,
                    donationDate);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your donation outcome has been submitted successfully.");

        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());
        }

        return "redirect:/donor/requests";
    }
}
