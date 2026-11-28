package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.service.DonationHistoryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Donor's own donation history (under /donor/**, USER role only). */
@Controller
public class DonorHistoryController {

    private final DonationHistoryService historyService;

    public DonorHistoryController(DonationHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/donor/history")
    public String history(Authentication authentication, Model model) {
        model.addAttribute("history", historyService.getHistory(authentication.getName()));
        return "donor/history";
    }
}
