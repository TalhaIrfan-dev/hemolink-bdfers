
package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.service.DonorMatchingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/requests/{requestId}/matches")
public class MatchingController {

    private final DonorMatchingService matchingService;

    public MatchingController(DonorMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping
    public String showMatches(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        model.addAttribute(
                "matches",
                matchingService.getMatches(
                        requestId, userDetails.getUsername())
        );

        model.addAttribute("requestId", requestId);

        return "requester/matching-results";
    }

    @PostMapping("/refresh")
    public String refreshMatches(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        matchingService.refreshMatches(
                requestId, userDetails.getUsername());

        return "redirect:/requests/" + requestId + "/matches?refreshed";
    }
}
