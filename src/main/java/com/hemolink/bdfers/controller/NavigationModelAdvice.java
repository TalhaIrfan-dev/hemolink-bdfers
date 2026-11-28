package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.dto.response.CurrentUserView;
import com.hemolink.bdfers.enums.UserRole;
import com.hemolink.bdfers.repository.UserRepository;
import com.hemolink.bdfers.service.NotificationService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/**
 * Adds navigation data to every page model:
 * <ul>
 *   <li>{@code unreadNotificationCount} - for the bell and sidebar badge (FR-13);</li>
 *   <li>{@code currentUser} - name, initials and role for the top bar (Module 3.1).</li>
 * </ul>
 * Guests get 0 and null, and no database query is made.
 */
@ControllerAdvice
public class NavigationModelAdvice {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NavigationModelAdvice(NotificationService notificationService,
                                 UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount(Principal principal) {
        return principal == null ? 0 : notificationService.unreadCount(principal.getName());
    }

    @ModelAttribute("currentUser")
    public CurrentUserView currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName())
                .map(user -> new CurrentUserView(
                        user.getFullName(), user.getRole() == UserRole.ADMIN))
                .orElse(null);
    }
}
