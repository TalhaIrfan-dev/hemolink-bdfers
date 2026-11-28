package com.hemolink.bdfers.service;

import com.hemolink.bdfers.enums.NotificationType;

import com.hemolink.bdfers.dto.response.DonorContact;
import com.hemolink.bdfers.dto.response.MatchSummary;
import com.hemolink.bdfers.dto.response.MatchingResultsView;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.entity.Notification;
import com.hemolink.bdfers.enums.AvailabilityStatus;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.MatchingStatus;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.enums.RequestStage;
import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.exception.NotFoundException;
import com.hemolink.bdfers.repository.BloodRequestRepository;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Donor matching (FR-10, FR-11), matching results (FR-12) and
 * donor-controlled contact disclosure (FR-15 as changed by D-16).
 */
@Service
public class DonorMatchingService {

    private final BloodRequestRepository requestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final DonorMatchRepository donorMatchRepository;
    private final NotificationService notificationService;

    public DonorMatchingService(
            BloodRequestRepository requestRepository,
            DonorProfileRepository donorProfileRepository,
            DonorMatchRepository donorMatchRepository,
            NotificationService notificationService) {

        this.requestRepository = requestRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.donorMatchRepository = donorMatchRepository;
        this.notificationService = notificationService;
    }

    // ---------------- DONOR MATCHING ----------------

    /**
     * Finds potential donors for a request and creates a match and an
     * in-app alert for each new one.
     *
     * Rules:
     * <ul>
     *   <li>Request must be active (Pending or Approved).</li>
     *   <li>Normal requests must be approved first; Critical requests are
     *       matched immediately (FR-11, D-15).</li>
     *   <li>Donor blood group must be compatible (FR-10).</li>
     *   <li>Same city, Available, active profile and account (FR-05, FR-17).</li>
     *   <li>Requester's own donor profile is excluded (FR-11).</li>
     *   <li>Eligibility is advisory and does not filter matching (D-01).</li>
     *   <li>Donors already matched are not matched again.</li>
     * </ul>
     */
    @Transactional
    public void generateMatches(Long requestId) {

        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException("Blood request not found."));

        RequestStatusPolicy.requireActive(
                request.getAdminStatus(),
                "Matching is not available because this request is "
                + request.getAdminStatus().getLabel().toLowerCase() + ".");

        if (request.getUrgency() == RequestUrgency.NORMAL
                && request.getAdminStatus() != RequestAdminStatus.APPROVED) {

            throw new BusinessRuleException(
                    "Normal requests must be approved by an administrator before matching.");
        }

        Set<BloodGroup> compatibleGroups =
                BloodCompatibility.compatibleDonorGroups(request.getBloodGroup());

        List<DonorProfile> candidates =
                donorProfileRepository.findMatchCandidates(
                        compatibleGroups,
                        request.getCity().trim(),
                        AvailabilityStatus.AVAILABLE,
                        request.getRequester().getId());

        // Load existing matches once instead of one query per donor.
        Set<Long> matchedDonorIds = new HashSet<>(
                donorMatchRepository.findDonorIdsByRequestId(requestId));

        List<DonorMatch> newMatches = new ArrayList<>();
        List<Notification> newAlerts = new ArrayList<>();

        for (DonorProfile donor : candidates) {

            // add() returns false if the donor is already matched.
            if (!matchedDonorIds.add(donor.getId())) {
                continue;
            }

            DonorMatch match = new DonorMatch();
            match.setBloodRequest(request);
            match.setDonor(donor);
            match.setDonorResponse(DonorResponse.PENDING);
            match.setDonorConsent(false);
            newMatches.add(match);

            // FR-13: in-app alert for the matched donor.
            Notification alert = notificationService.build(
                    donor.getUser(), request, NotificationType.MATCH, null);
            alert.setMessage(
                    "A new " + request.getUrgency().getLabel().toLowerCase()
                    + " blood request (" + formatRequestId(requestId) + ", "
                    + request.getBloodGroup().getLabel() + " in "
                    + request.getCity() + ") matches your donor profile. "
                    + "Please review it in Matched Requests.");
            newAlerts.add(alert);
        }

        donorMatchRepository.saveAll(newMatches);
        notificationService.saveAll(newAlerts);

        request.setMatchingStatus(
                matchedDonorIds.isEmpty()
                        ? MatchingStatus.NO_MATCHES
                        : MatchingStatus.MATCHES_FOUND);

        requestRepository.save(request);
    }

    // ---------------- MATCHING RESULTS (REQUESTER) ----------------

    /**
     * Masked matching results for the requester's own request (FR-12).
     *
     * @param filter optional donor-response filter; null shows all
     */
    @Transactional(readOnly = true)
    public MatchingResultsView getMatchingResults(
            Long requestId, String requesterEmail, DonorResponse filter) {

        BloodRequest request = findOwnRequest(requestId, requesterEmail);

        boolean requestActive =
                RequestStatusPolicy.isActive(request.getAdminStatus());

        List<DonorMatch> matches =
                donorMatchRepository.findAllWithDonorByRequestId(requestId);

        long awaiting = 0;
        long accepted = 0;
        long declined = 0;
        long shared = 0;
        long unavailable = 0;

        List<MatchSummary> summaries = new ArrayList<>();
        int number = 0;

        for (DonorMatch match : matches) {
            number++;

            DonorResponse response = match.getDonorResponse();
            boolean contactShared = requestActive && isContactShared(match);

            // A deactivated donor who had not declined is shown as
            // "No longer available" (owner decision, Module 2.5).
            boolean donorUnavailable = !match.getDonor().isActive()
                    && response != DonorResponse.DECLINED;

            if (donorUnavailable) {
                unavailable++;
            } else {
                switch (response) {
                    case PENDING -> awaiting++;
                    case ACCEPTED -> accepted++;
                    case DECLINED -> declined++;
                }
            }
            if (contactShared) {
                shared++;
            }

            if (filter == null || (filter == response && !donorUnavailable)) {
                summaries.add(new MatchSummary(
                        number,
                        match.getDonor().getBloodGroup().getLabel(),
                        match.getDonor().getCity(),
                        response,
                        contactShared,
                        donorUnavailable));
            }
        }

        String refreshBlockedReason = refreshBlockedReason(request);

        return new MatchingResultsView(
                request.getId(),
                request.getBloodGroup().getLabel(),
                request.getCity(),
                request.getUrgency().getLabel(),
                request.getAdminStatus().getLabel(),
                request.getMatchingStatus().getLabel(),
                request.getUnitsRequired(),
                request.getFulfilledUnits() == null ? 0 : request.getFulfilledUnits(),
                refreshBlockedReason == null,
                refreshBlockedReason,
                matches.size(),
                awaiting,
                accepted,
                declined,
                shared,
                unavailable,
                filter,
                summaries,
                RequestStage.of(request.getAdminStatus(), request.getUrgency(),
                        request.getMatchingStatus()),
                request.getUrgency() == RequestUrgency.CRITICAL);
    }

    // ---------------- REFRESH MATCHES ----------------

    @Transactional
    public void refreshMatches(Long requestId, String requesterEmail) {

        findOwnRequest(requestId, requesterEmail);

        // generateMatches() also checks status and urgency rules.
        generateMatches(requestId);
    }

    // ---------------- SHARED DONOR CONTACTS ----------------

    /**
     * Donor contacts visible to the requester (FR-15 as changed by D-16):
     * the donor has accepted AND explicitly consented, and the request is
     * still active. No administrator approval is required. Contacts are
     * hidden again once the request is closed, rejected or fulfilled.
     */
    @Transactional(readOnly = true)
    public List<DonorContact> getSharedContacts(
            Long requestId, String requesterEmail) {

        BloodRequest request = findOwnRequest(requestId, requesterEmail);

        if (!RequestStatusPolicy.isActive(request.getAdminStatus())) {
            return List.of();
        }

        return donorMatchRepository
                .findMatchesWithDonorDetails(requestId)
                .stream()
                .filter(DonorMatchingService::isContactShared)
                .map(match -> new DonorContact(
                        match.getDonor().getUser().getFullName(),
                        match.getDonor().getUser().getPhone(),
                        match.getDonor().getBloodGroup().getLabel(),
                        match.getDonor().getCity(),
                        match.getConsentAt()))
                .toList();
    }

    /** True if the request is no longer active, so contacts are hidden. */
    @Transactional(readOnly = true)
    public boolean isRequestInactive(Long requestId, String requesterEmail) {
        BloodRequest request = findOwnRequest(requestId, requesterEmail);
        return !RequestStatusPolicy.isActive(request.getAdminStatus());
    }

    // ---------------- HELPERS ----------------

    private BloodRequest findOwnRequest(Long requestId, String requesterEmail) {
        return requestRepository
                .findByIdAndRequesterEmail(requestId, requesterEmail)
                .orElseThrow(() ->
                        new NotFoundException("Blood request not found."));
    }

    /**
     * Donor has accepted and explicitly consented (D-16), and the donor
     * profile is still active: contacts of deactivated donors are hidden
     * (owner decision, Module 2.5).
     */
    private static boolean isContactShared(DonorMatch match) {
        return match.getDonorResponse() == DonorResponse.ACCEPTED
                && match.isDonorConsent()
                && match.getDonor().isActive();
    }

    /** Null if refreshing is allowed, otherwise the reason it is not. */
    private static String refreshBlockedReason(BloodRequest request) {

        if (!RequestStatusPolicy.isActive(request.getAdminStatus())) {
            return "Matching is not available because this request is "
                    + request.getAdminStatus().getLabel().toLowerCase() + ".";
        }

        if (request.getUrgency() == RequestUrgency.NORMAL
                && request.getAdminStatus() != RequestAdminStatus.APPROVED) {
            return "Matching starts after an administrator approves this request.";
        }

        return null;
    }

    private static String formatRequestId(Long id) {
        return String.format("REQ-%04d", id);
    }
}
