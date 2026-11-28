package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.dto.request.AccountProfileRequest;
import com.hemolink.bdfers.dto.request.PasswordChangeRequest;
import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Account settings for every signed-in user, including administrators. */
@Controller
@RequestMapping("/account")
public class AccountController {

    private static final String VIEW = "account";

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String show(Authentication authentication, Model model) {
        AppUser account = userService.getAccount(authentication.getName());

        AccountProfileRequest profile = new AccountProfileRequest();
        profile.setFullName(account.getFullName());
        profile.setPhone(account.getPhone());

        model.addAttribute("account", account);
        model.addAttribute("profileForm", profile);
        model.addAttribute("passwordForm", new PasswordChangeRequest());
        return VIEW;
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") AccountProfileRequest profileForm,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("account", userService.getAccount(authentication.getName()));
            model.addAttribute("passwordForm", new PasswordChangeRequest());
            return VIEW;
        }

        userService.updateProfile(authentication.getName(), profileForm);
        redirectAttributes.addFlashAttribute("successMessage", "Your details have been updated.");
        return "redirect:/account";
    }

    @PostMapping("/password")
    public String changePassword(
            @Valid @ModelAttribute("passwordForm") PasswordChangeRequest passwordForm,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!result.hasFieldErrors("newPassword")
                && !result.hasFieldErrors("confirmPassword")
                && !passwordForm.getNewPassword().equals(passwordForm.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch",
                    "The two new passwords do not match.");
        }

        if (!result.hasErrors()) {
            try {
                userService.changePassword(
                        authentication.getName(),
                        passwordForm.getCurrentPassword(),
                        passwordForm.getNewPassword());

                redirectAttributes.addFlashAttribute("successMessage", "Your password has been changed.");
                return "redirect:/account";

            } catch (BusinessRuleException ex) {
                String field = ex.getMessage().startsWith("Your current password")
                        ? "currentPassword" : "newPassword";
                result.rejectValue(field, "invalid", ex.getMessage());
            }
        }

        // Never send passwords back to the page.
        passwordForm.setCurrentPassword(null);
        passwordForm.setNewPassword(null);
        passwordForm.setConfirmPassword(null);

        AppUser account = userService.getAccount(authentication.getName());
        AccountProfileRequest profile = new AccountProfileRequest();
        profile.setFullName(account.getFullName());
        profile.setPhone(account.getPhone());

        model.addAttribute("account", account);
        model.addAttribute("profileForm", profile);
        return VIEW;
    }
}
