package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.dto.response.AdminDashboard;
import com.hemolink.bdfers.dto.response.TrendData;
import com.hemolink.bdfers.service.AdminService;
import com.hemolink.bdfers.service.DashboardService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final DashboardService dashboardService;

    public AdminController(AdminService adminService, DashboardService dashboardService) {
        this.adminService = adminService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // D-12: all counts come from the database.
        AdminDashboard adminDashboard = dashboardService.getAdminDashboard();
        model.addAttribute("adminDashboard", adminDashboard);
        model.addAttribute("stats", adminDashboard.getStats());

        return "admin/dashboard";
    }

    /** Trend chart data for all requests (JSON for Chart.js). */
    @GetMapping("/dashboard/trend")
    @ResponseBody
    public TrendData dashboardTrend(
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "year", required = false) Integer year) {

        return dashboardService.getTrend(null, period, year);
    }

    // Donor Directory: see DonorAdminController (/admin/donors).

    // ---------------- BLOOD REQUEST MANAGEMENT (FR-16) ----------------

    /**
     * All requests with filters and paging. With no status parameter the
     * Pending review queue is shown (Critical first, then oldest first);
     * status=ALL shows every request.
     */
    @GetMapping("/blood-requests")
    public String bloodRequests(
            @RequestParam(name = "status", required = false) String statusParam,
            @RequestParam(name = "urgency", required = false) RequestUrgency urgency,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        RequestAdminStatus status = parseStatus(statusParam);

        model.addAttribute("requests",
                adminService.searchRequests(status, urgency, city, page));

        model.addAttribute("statusFilter", status == null ? "ALL" : status.name());
        model.addAttribute("urgencyFilter", urgency);
        model.addAttribute("cityFilter", city == null ? "" : city.trim());
        model.addAttribute("statuses", RequestAdminStatus.values());
        model.addAttribute("urgencies", RequestUrgency.values());
        model.addAttribute("cities", adminService.getRequestCities());

        return "admin/blood-requests";
    }

    @GetMapping("/blood-requests/{id}")
    public String bloodRequestDetail(@PathVariable Long id, Model model) {

        model.addAttribute("detail", adminService.getRequestDetail(id));

        return "admin/blood-request-detail";
    }

    @PostMapping("/blood-requests/{id}/review")
    public String reviewBloodRequest(
            @PathVariable Long id,
            @RequestParam RequestAdminStatus decision,
            @RequestParam(required = false) String rejectionReason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.reviewBloodRequest(
                    id, decision, rejectionReason, authentication.getName());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    decision == RequestAdminStatus.APPROVED
                            ? "Blood request approved."
                            : "Blood request rejected.");

        } catch (IllegalArgumentException | IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/admin/blood-requests/" + id;
    }

    @PostMapping("/blood-requests/{id}/close")
    public String closeBloodRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.closeRequest(id, reason, authentication.getName());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Blood request closed.");

        } catch (IllegalArgumentException | IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/admin/blood-requests/" + id;
    }

    /** Null means "all statuses"; missing or unknown means Pending. */
    private static RequestAdminStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return RequestAdminStatus.PENDING;
        }
        if ("ALL".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return RequestAdminStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return RequestAdminStatus.PENDING;
        }
    }

    // ---------------- CONTACT DISCLOSURE LOG ----------------

    /**
     * Read-only log of donor contact disclosures (D-16). Disclosure is
     * controlled by the donor's acceptance and consent; administrators
     * can see who shared contact details with whom, and when.
     */
    @GetMapping("/disclosures")
    public String disclosures(Model model) {

        model.addAttribute(
                "disclosures",
                adminService.getDisclosureLog());

        return "admin/disclosures";
    }
}
