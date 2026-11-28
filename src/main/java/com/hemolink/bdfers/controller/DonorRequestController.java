
package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.dto.request.HealthCheckRequest;
import com.hemolink.bdfers.enums.DonationOutcome;
import com.hemolink.bdfers.enums.DonorRequestTab;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.service.DonationReportingService;
import com.hemolink.bdfers.service.DonorAcceptanceService;
import com.hemolink.bdfers.service.DonorProfileService;
import com.hemolink.bdfers.service.DonorRequestService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/donor/requests")
public class DonorRequestController {

    private static final String ACCEPT_VIEW = "donor/accept-health-check";

    private final DonorRequestService donorRequestService;
    private final DonationReportingService donationReportingService;
    private final DonorAcceptanceService acceptanceService;
    private final DonorProfileService donorProfileService;

    public DonorRequestController(
            DonorRequestService donorRequestService,
            DonationReportingService donationReportingService,
            DonorAcceptanceService acceptanceService,
            DonorProfileService donorProfileService) {

        this.donorRequestService = donorRequestService;
        this.donationReportingService = donationReportingService;
        this.acceptanceService = acceptanceService;
        this.donorProfileService = donorProfileService;
    }

    /** Matched Requests with tabs (Module 3.5): ?tab=waiting|accepted|past. */
    @GetMapping
    public String myRequests(
            @RequestParam(name = "tab", required = false) String tab,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();

        model.addAttribute(
                "view",
                donorRequestService.getMyRequestsView(
                        email,
                        DonorRequestTab.fromParam(tab),
                        donationReportingService.getReportedMatchIds(email)));

        model.addAttribute("today", LocalDate.now());

        model.addAttribute(
                "deactivationNotice",
                donorProfileService.getDeactivationNotice(email));

        return "donor/requests";
    }

    // ---------------- ACCEPT WITH HEALTH CHECK (D-01) ----------------

    /** Step 1: health-check form for accepting a request. */
    @GetMapping("/{matchId}/accept")
    public String showHealthCheck(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();

        try {
            model.addAttribute("context",
                    acceptanceService.getContext(matchId, email));
            model.addAttribute("healthCheck",
                    acceptanceService.prefilledForm(matchId, email));
            return ACCEPT_VIEW;

        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/donor/requests";
        }
    }

    /** Step 2: show the advice for the values entered (nothing saved yet). */
    @PostMapping("/{matchId}/accept/check")
    public String checkHealth(
            @PathVariable Long matchId,
            @Valid @ModelAttribute("healthCheck") HealthCheckRequest healthCheck,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        model.addAttribute("context", acceptanceService.getContext(matchId, email));

        if (bindingResult.hasErrors()) {
            return ACCEPT_VIEW;
        }

        try {
            model.addAttribute("advice",
                    acceptanceService.preview(matchId, email, healthCheck));
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return ACCEPT_VIEW;
    }

    /** Step 3: save the health check and accept the request. */
    @PostMapping("/{matchId}/accept/confirm")
    public String confirmAccept(
            @PathVariable Long matchId,
            @Valid @ModelAttribute("healthCheck") HealthCheckRequest healthCheck,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();

        if (bindingResult.hasErrors()) {
            model.addAttribute("context", acceptanceService.getContext(matchId, email));
            return ACCEPT_VIEW;
        }

        try {
            var advice = acceptanceService.accept(matchId, email, healthCheck);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    advice.isOk()
                            ? "You have accepted this request. You can now choose whether to share your contact details."
                            : "You have accepted this request against the guidance. Please take care, and follow the advice of the hospital or blood bank.");

            return "redirect:/donor/requests";

        } catch (BusinessRuleException ex) {
            model.addAttribute("context", acceptanceService.getContext(matchId, email));
            model.addAttribute("errorMessage", ex.getMessage());
            try {
                model.addAttribute("advice",
                        acceptanceService.preview(matchId, email, healthCheck));
            } catch (BusinessRuleException ignored) {
                // Show the form again with the error message only.
            }
            return ACCEPT_VIEW;
        }
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
                    response == DonorResponse.DECLINED
                            ? "You declined this request."
                            : "Your response has been recorded.");

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
                    "Your name and phone number are now shared with the requester "
                    + "for as long as the request stays active.");

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
