package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.dto.response.GuidanceSettingRow;
import com.hemolink.bdfers.enums.GuidanceKey;
import com.hemolink.bdfers.service.GuidanceSettingsService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin page for the donation guidance values (D-02, D-03). Protected by
 * the /admin/** rule in SecurityConfig. Form fields are named
 * "value_" + GuidanceKey name; an empty field switches that check off.
 */
@Controller
@RequestMapping("/admin/guidance-settings")
public class GuidanceSettingsController {

    private static final String FIELD_PREFIX = "value_";
    private static final String VIEW = "admin/guidance-settings";

    private final GuidanceSettingsService settingsService;

    public GuidanceSettingsController(GuidanceSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public String show(Model model) {
        model.addAttribute("rows", settingsService.listForAdmin());
        model.addAttribute("changes", settingsService.recentChanges());
        model.addAttribute("errors", Map.of());
        return VIEW;
    }

    @PostMapping
    public String save(
            @RequestParam Map<String, String> params,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        Map<GuidanceKey, String> submitted = new EnumMap<>(GuidanceKey.class);
        for (GuidanceKey key : GuidanceKey.values()) {
            submitted.put(key, params.getOrDefault(FIELD_PREFIX + key.name(), ""));
        }

        Map<GuidanceKey, String> errors =
                settingsService.update(submitted, authentication.getName());

        if (errors.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Donation guidance settings saved.");
            return "redirect:/admin/guidance-settings";
        }

        // Show the values the admin typed, with an error next to each problem.
        List<GuidanceSettingRow> rows = settingsService.listForAdmin()
                .stream()
                .map(row -> new GuidanceSettingRow(
                        row.getKey(),
                        submitted.get(row.getKey()).trim(),
                        row.getUpdatedByName(),
                        row.getUpdatedAt()))
                .toList();

        Map<String, String> errorsByName = new HashMap<>();
        errors.forEach((key, message) -> errorsByName.put(key.name(), message));

        model.addAttribute("rows", rows);
        model.addAttribute("changes", settingsService.recentChanges());
        model.addAttribute("errors", errorsByName);
        model.addAttribute("errorMessage",
                "Some values need correcting. Nothing was saved.");
        return VIEW;
    }
}
