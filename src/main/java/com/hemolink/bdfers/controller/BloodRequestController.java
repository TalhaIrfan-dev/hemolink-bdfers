
package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.dto.request.BloodRequestRequest;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.service.BloodRequestService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/requests")
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;

    public BloodRequestController(BloodRequestService bloodRequestService) {
        this.bloodRequestService = bloodRequestService;
    }

    @GetMapping("/new")
    public String showRequestForm(Model model) {

        model.addAttribute("request", new BloodRequestRequest());
        addFormOptions(model);

        return "requester/request-form";
    }

    @PostMapping
    public String submitRequest(
            @Valid @ModelAttribute("request") BloodRequestRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "requester/request-form";
        }

        bloodRequestService.createRequest(request, userDetails.getUsername());

        return "redirect:/requests?success";
    }

    @GetMapping
    public String showMyRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        model.addAttribute(
                "requests",
                bloodRequestService.getMyRequests(userDetails.getUsername())
        );

        return "requester/my-requests";
    }

    @PostMapping("/{id}/close")
    public String closeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        try {
            bloodRequestService.closeRequest(id, userDetails.getUsername());
            return "redirect:/requests?closed";

        } catch (IllegalArgumentException | IllegalStateException exception) {

            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute(
                    "requests",
                    bloodRequestService.getMyRequests(userDetails.getUsername())
            );

            return "requester/my-requests";
        }
    }

    private void addFormOptions(Model model) {
        model.addAttribute("bloodGroups", BloodGroup.values());
        model.addAttribute("urgencyOptions", RequestUrgency.values());
    }
}
