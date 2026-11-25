
package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.service.DonorRequestService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/donor/requests")
public class DonorRequestController {

    private final DonorRequestService donorRequestService;

    public DonorRequestController(
            DonorRequestService donorRequestService) {
        this.donorRequestService = donorRequestService;
    }

    @GetMapping
    public String myRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        model.addAttribute(
                "matches",
                donorRequestService.getMyMatchedRequests(
                        userDetails.getUsername()));

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
                    matchId, response, userDetails.getUsername());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Your response has been recorded.");

        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
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
                    matchId, userDetails.getUsername());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your contact-sharing consent has been recorded. "
                    + "Administrator approval is still required.");

        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/donor/requests";
    }
}
