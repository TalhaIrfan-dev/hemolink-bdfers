package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * In-app notifications for donors, requesters and administrators (FR-13).
 * Every signed-in user can see only their own notifications.
 */
@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String list(
            @RequestParam(name = "unread", defaultValue = "false") boolean unreadOnly,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Authentication authentication,
            Model model) {

        model.addAttribute("notifications",
                notificationService.getPage(authentication.getName(), unreadOnly, page));
        model.addAttribute("unreadOnly", unreadOnly);

        return "notifications";
    }

    /** Marks the notification read and opens the page it refers to. */
    @PostMapping("/{id}/open")
    public String open(@PathVariable Long id, Authentication authentication) {
        return "redirect:" + notificationService.open(id, authentication.getName());
    }

    @PostMapping("/read-all")
    public String markAllRead(
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        notificationService.markAllRead(authentication.getName());
        redirectAttributes.addFlashAttribute(
                "successMessage", "All notifications marked as read.");

        return "redirect:/notifications";
    }
}
