package com.hemolink.bdfers.service;

import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.exception.NotFoundException;

import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonationRecord;
import com.hemolink.bdfers.entity.DonorHealthCheck;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.enums.*;
import com.hemolink.bdfers.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemolink.bdfers.dto.response.AdminDashboardStats;
import com.hemolink.bdfers.dto.response.AdminMatchRow;
import com.hemolink.bdfers.dto.response.AdminRequestDetailView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final DonorProfileRepository donorProfileRepository;
    private final DonorMatchRepository donorMatchRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorHealthCheckRepository healthCheckRepository;
    private final DonationRecordRepository donationRecordRepository;
    private final UserRepository userRepository;
    private final DonorMatchingService matchingService;
    private final NotificationService notificationService;
    private final Clock clock;

    public AdminService(
            DonorProfileRepository donorProfileRepository,
            DonorMatchRepository donorMatchRepository,
            BloodRequestRepository bloodRequestRepository,
            DonorHealthCheckRepository healthCheckRepository,
            DonationRecordRepository donationRecordRepository,
            UserRepository userRepository,
            DonorMatchingService matchingService,
            NotificationService notificationService,
            Clock clock) {

        this.donorProfileRepository = donorProfileRepository;
        this.donorMatchRepository = donorMatchRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.donationRecordRepository = donationRecordRepository;
        this.userRepository = userRepository;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    // ---------------- DASHBOARD (D-12) ----------------

    @Transactional(readOnly = true)
    public AdminDashboardStats getDashboardStats() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now(clock).minusDays(30);

        return new AdminDashboardStats(
                donorProfileRepository.countByActiveTrue(),
                donorProfileRepository.countByActiveTrueAndAvailability(
                        AvailabilityStatus.AVAILABLE),
                healthCheckRepository
                        .countByAcceptedAgainstAdviceTrueAndCreatedAtAfter(thirtyDaysAgo),
                donorProfileRepository.countByActiveFalse(),
                bloodRequestRepository.countByAdminStatus(RequestAdminStatus.PENDING),
                bloodRequestRepository.countByAdminStatusAndUrgency(
                        RequestAdminStatus.PENDING, RequestUrgency.CRITICAL));
    }

    // ---------------- BLOOD REQUEST MANAGEMENT (FR-16) ----------------

    public static final int PAGE_SIZE = 20;

    private static final int MAX_REASON_LENGTH = 255;

    /**
     * Admin request list with optional filters. Pending requests are
     * ordered by urgency (Critical first), then oldest submission first,
     * as FR-16 requires. Other views show the newest first.
     */
    @Transactional(readOnly = true)
    public Page<BloodRequest> searchRequests(
            RequestAdminStatus status,
            RequestUrgency urgency,
            String city,
            int page) {

        Sort sort = status == RequestAdminStatus.PENDING
                ? Sort.by(Sort.Order.asc("urgency"), Sort.Order.asc("createdAt"))
                : Sort.by(Sort.Order.desc("createdAt"));

        String cityFilter = (city == null || city.isBlank()) ? null : city.trim();

        return bloodRequestRepository.search(
                status,
                urgency,
                cityFilter,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, sort));
    }

    @Transactional(readOnly = true)
    public List<String> getRequestCities() {
        return bloodRequestRepository.findDistinctCities();
    }

    /**
     * Approves or rejects a Pending request and records who did it and when.
     * A rejection requires a reason. Approving a Normal request starts
     * matching; Critical requests were already matched on submission.
     */
    @Transactional
    public void reviewBloodRequest(
            Long requestId,
            RequestAdminStatus decision,
            String rejectionReason,
            String adminEmail) {

        if (decision != RequestAdminStatus.APPROVED
                && decision != RequestAdminStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Invalid blood request decision.");
        }

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Blood request not found."));

        if (request.getAdminStatus() != RequestAdminStatus.PENDING) {
            throw new BusinessRuleException(
                    "This blood request has already been reviewed.");
        }

        // FR-19: only permitted status transitions are applied.
        RequestStatusPolicy.requireTransition(
                request.getAdminStatus(), decision);

        String reason = cleanReason(rejectionReason);

        if (decision == RequestAdminStatus.REJECTED && reason == null) {
            throw new IllegalArgumentException(
                    "Please provide a reason for rejecting the request.");
        }

        AppUser admin = findAdmin(adminEmail);

        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now(clock));

        if (decision == RequestAdminStatus.REJECTED) {

            request.setAdminStatus(RequestAdminStatus.REJECTED);
            request.setRejectionReason(reason);

            bloodRequestRepository.save(request);

            // Donors can no longer accept, and shared contact details are
            // hidden automatically because the request is inactive (D-15, D-16).
            notificationService.requestReviewed(request, false, reason);
            notificationService.requestEndedForDonors(
                    request, "was rejected by an administrator.", null);
            return;
        }

        request.setAdminStatus(RequestAdminStatus.APPROVED);
        request.setRejectionReason(null);

        bloodRequestRepository.save(request);

        notificationService.requestReviewed(request, true, null);

        // Critical requests were already matched on submission.
        // generateMatches() avoids duplicate donor matches and alerts
        // each newly matched donor.
        if (request.getUrgency() == RequestUrgency.NORMAL) {
            matchingService.generateMatches(requestId);
        }
    }

    /**
     * Closes a Pending or Approved request (FR-16, DEF-09). Administrators
     * must give a reason. Shared donor contacts are hidden automatically.
     */
    @Transactional
    public void closeRequest(Long requestId, String reason, String adminEmail) {

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException("Blood request not found."));

        RequestStatusPolicy.requireTransition(
                request.getAdminStatus(), RequestAdminStatus.CLOSED);

        String cleaned = cleanReason(reason);
        if (cleaned == null) {
            throw new IllegalArgumentException(
                    "Please provide a reason for closing the request.");
        }

        request.setAdminStatus(RequestAdminStatus.CLOSED);
        request.setCloseReason(cleaned);
        request.setClosedBy(findAdmin(adminEmail));
        request.setClosedAt(LocalDateTime.now(clock));

        bloodRequestRepository.save(request);

        notificationService.requestClosedByAdmin(request, cleaned);
        notificationService.requestEndedForDonors(request, "has been closed.", null);
    }

    /**
     * Full request detail for administrators: requester, every matched
     * donor with contact details, raw health-check values (D-04) and
     * donation reports.
     */
    @Transactional(readOnly = true)
    public AdminRequestDetailView getRequestDetail(Long requestId) {

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException("Blood request not found."));

        // Load the lazy associations shown on the page.
        request.getRequester().getFullName();
        if (request.getReviewedBy() != null) {
            request.getReviewedBy().getFullName();
        }
        if (request.getClosedBy() != null) {
            request.getClosedBy().getFullName();
        }

        Map<Long, DonorHealthCheck> healthChecks = new HashMap<>();
        healthCheckRepository.findAllByRequestId(requestId)
                .forEach(hc -> healthChecks.put(hc.getMatch().getId(), hc));

        Map<Long, DonationRecord> donations = new HashMap<>();
        donationRecordRepository.findAllByRequestId(requestId)
                .forEach(dr -> donations.put(dr.getMatch().getId(), dr));

        List<AdminMatchRow> rows = donorMatchRepository
                .findAllWithDonorByRequestId(requestId)
                .stream()
                .map(match -> new AdminMatchRow(
                        match.getDonor().getId(),
                        match.getDonor().getUser().getFullName(),
                        match.getDonor().getUser().getEmail(),
                        match.getDonor().getUser().getPhone(),
                        match.getDonor().getBloodGroup().getLabel(),
                        match.getDonor().getCity(),
                        match.getDonor().isActive(),
                        match.getDonorResponse().getLabel(),
                        match.isDonorConsent(),
                        match.getConsentAt(),
                        healthChecks.get(match.getId()),
                        donations.get(match.getId())))
                .toList();

        RequestAdminStatus status = request.getAdminStatus();

        return new AdminRequestDetailView(
                request,
                rows,
                status == RequestAdminStatus.PENDING,
                RequestStatusPolicy.canTransition(status, RequestAdminStatus.CLOSED),
                status == RequestAdminStatus.PENDING
                        && request.getUrgency() == RequestUrgency.CRITICAL);
    }

    private AppUser findAdmin(String adminEmail) {
        return userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new NotFoundException("Administrator not found."));
    }

    /** Trims a reason; returns null if blank; rejects over-long text. */
    private static String cleanReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        String trimmed = reason.trim();
        if (trimmed.length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException(
                    "The reason cannot exceed " + MAX_REASON_LENGTH + " characters.");
        }
        return trimmed;
    }

    // ---------------- CONTACT DISCLOSURE LOG ----------------

    /**
     * Read-only log of contact disclosures (D-16): every match where the
     * donor accepted and consented to share their contact details.
     * Administrators no longer approve disclosures.
     */
    @Transactional(readOnly = true)
    public List<DonorMatch> getDisclosureLog() {
        return donorMatchRepository.findDisclosureLog(DonorResponse.ACCEPTED);
    }
}
