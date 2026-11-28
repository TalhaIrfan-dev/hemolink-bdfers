package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.service.DonorMatchingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests/{requestId}/matches")
public class MatchingController {

    private final DonorMatchingService matchingService;

    public MatchingController(DonorMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    /**
     * Shows masked matching results with counts (FR-12). The optional
     * {@code response} parameter filters by donor response
     * (PENDING, ACCEPTED, DECLINED). A request that does not exist or
     * belongs to another user results in a 404 page (FR-03).
     */
    @GetMapping
    public String showMatches(
            @PathVariable Long requestId,
            @RequestParam(name = "response", required = false) DonorResponse response,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        model.addAttribute(
                "view",
                matchingService.getMatchingResults(
                        requestId, userDetails.getUsername(), response));

        model.addAttribute("responseFilters", DonorResponse.values());

        return "requester/matching-results";
    }

    /**
     * Refreshes matching. If matching is not currently allowed (for example
     * a Normal request still awaiting approval, or a closed request), the
     * user is returned to the results page with an explanation instead of
     * an error page (DEF-03).
     */
    @PostMapping("/refresh")
    public String refreshMatches(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            matchingService.refreshMatches(
                    requestId, userDetails.getUsername());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Matching results refreshed successfully.");

        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/requests/" + requestId + "/matches";
    }

    @GetMapping("/contacts")
    public String approvedContacts(
            @PathVariable Long requestId,
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "contacts",
                matchingService.getSharedContacts(
                        requestId, authentication.getName()));

        model.addAttribute(
                "requestInactive",
                matchingService.isRequestInactive(
                        requestId, authentication.getName()));

        model.addAttribute("requestId", requestId);

        return "requester/approved-contacts";
    }
}
