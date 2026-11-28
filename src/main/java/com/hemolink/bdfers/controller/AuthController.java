package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.dto.request.RegisterRequest;
import com.hemolink.bdfers.dto.response.TrendData;
import com.hemolink.bdfers.exception.DuplicateEmailException;
import com.hemolink.bdfers.service.DashboardService;
import com.hemolink.bdfers.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;
    private final DashboardService dashboardService;

    public AuthController(UserService userService, DashboardService dashboardService) {
        this.userService = userService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String home() {
        // Signed-in users go to their dashboard; others are sent to login
        // by Spring Security.
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerUser(request);
            return "redirect:/login?registered";

        } catch (DuplicateEmailException ex) {
            // FR-01: show the message next to the email field.
            result.rejectValue("email", "duplicate", ex.getMessage());
            return "auth/register";

        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    /**
     * User dashboard: requester and donor sections from real data
     * (Module 2.7). Administrators are sent to the admin dashboard.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        if (isAdmin(authentication)) {
            return "redirect:/admin/dashboard";
        }

        String email = authentication.getName();
        model.addAttribute("requesterDashboard", dashboardService.getRequesterDashboard(email));
        model.addAttribute("donorDashboard", dashboardService.getDonorDashboard(email));

        return "dashboard";
    }

    /**
     * Trend chart data for the signed-in user's own requests (JSON for
     * Chart.js). period = WEEK, MONTH or YEAR; year applies to YEAR.
     */
    @GetMapping("/dashboard/trend")
    @ResponseBody
    public TrendData dashboardTrend(
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "year", required = false) Integer year,
            Authentication authentication) {

        return dashboardService.getTrend(authentication.getName(), period, year);
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    /**
     * Shown by Spring Security when a signed-in user opens a page their
     * role does not allow (FR-03).
     */
    @RequestMapping("/access-denied")
    public String accessDenied(Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("status", 403);
        model.addAttribute("errorTitle", "Access denied");
        model.addAttribute("errorDetail",
                "You do not have permission to view this page.");
        return "error";
    }
}
