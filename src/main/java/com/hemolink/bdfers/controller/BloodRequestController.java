
package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.dto.request.BloodRequestRequest;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.enums.RequestListTab;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.service.BloodRequestService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "requester/request-form";
        }

        var saved = bloodRequestService.createRequest(request, userDetails.getUsername());

        redirectAttributes.addFlashAttribute("successMessage",
                request.getUrgency() == RequestUrgency.CRITICAL
                        ? "Critical blood request submitted. Matching has started."
                        : "Blood request submitted. Matching starts after an administrator approves it.");

        return "redirect:/requests/" + saved.getId();
    }

    /**
     * "My Requests" with status tabs (Module 3.4): ?tab=all|active|completed|closed.
     * Filtering is done on the server; an unknown tab shows All.
     */
    @GetMapping
    public String showMyRequests(
            @RequestParam(name = "tab", required = false) String tab,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        model.addAttribute(
                "view",
                bloodRequestService.getMyRequests(
                        userDetails.getUsername(), RequestListTab.fromParam(tab)));

        return "requester/my-requests";
    }

    /** Requester's detail page for one of their own requests. */
    @GetMapping("/{id}")
    public String requestDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        model.addAttribute(
                "detail",
                bloodRequestService.getRequestDetail(id, userDetails.getUsername()));

        return "requester/request-detail";
    }

    /** Close (cancel) the requester's own request; the reason is optional (D-13). */
    @PostMapping("/{id}/close")
    public String closeRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            bloodRequestService.closeRequest(id, userDetails.getUsername(), reason);

            redirectAttributes.addFlashAttribute("successMessage", "Your blood request has been closed.");

        } catch (IllegalArgumentException | IllegalStateException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", exception.getMessage());
        }

        return "redirect:/requests/" + id;
    }

    private void addFormOptions(Model model) {
        model.addAttribute("bloodGroups", BloodGroup.values());
        model.addAttribute("urgencyOptions", RequestUrgency.values());
    }
}
