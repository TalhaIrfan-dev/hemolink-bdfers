
package com.hemolink.bdfers.service;

import com.hemolink.bdfers.entity.*;
import com.hemolink.bdfers.enums.*;
import com.hemolink.bdfers.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DonorMatchingService {

    private final BloodRequestRepository requestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final DonorMatchRepository donorMatchRepository;
    private final NotificationRepository notificationRepository;

    public DonorMatchingService(
            BloodRequestRepository requestRepository,
            DonorProfileRepository donorProfileRepository,
            DonorMatchRepository donorMatchRepository,
            NotificationRepository notificationRepository) {

        this.requestRepository = requestRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.donorMatchRepository = donorMatchRepository;
        this.notificationRepository = notificationRepository;
    }

    // ---------------- DONOR MATCHING ----------------

    @Transactional
    public void generateMatches(Long requestId) {

        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Blood request not found"));

        // Prevent matching for inactive requests.
        if (request.getAdminStatus() == RequestAdminStatus.CLOSED
                || request.getAdminStatus() == RequestAdminStatus.REJECTED
                || request.getAdminStatus() == RequestAdminStatus.FULFILLED) {

            throw new IllegalStateException(
                    "Matching is not allowed for this request.");
        }

        // Normal requests require administrator approval first.
        // Critical requests can be matched immediately while PENDING.
        if (request.getUrgency() == RequestUrgency.NORMAL
                && request.getAdminStatus() != RequestAdminStatus.APPROVED) {

            throw new IllegalStateException(
                    "Normal requests must be approved by an administrator before matching.");
        }

        List<DonorProfile> eligibleDonors =
                donorProfileRepository
                        .findAllByBloodGroupAndCityIgnoreCaseAndAvailabilityAndEligibilityStatusAndActiveTrue(
                                request.getBloodGroup(),
                                request.getCity().trim(),
                                AvailabilityStatus.AVAILABLE,
                                EligibilityStatus.ELIGIBLE
                        );

        for (DonorProfile donor : eligibleDonors) {

            // Do not match a requester with their own donor profile.
            if (donor.getUser().getId()
                    .equals(request.getRequester().getId())) {
                continue;
            }

            // Prevent duplicate matches.
            boolean alreadyMatched =
                    donorMatchRepository.existsByBloodRequestIdAndDonorId(
                            requestId, donor.getId());

            if (alreadyMatched) {
                continue;
            }

            DonorMatch match = new DonorMatch();

            match.setBloodRequest(request);
            match.setDonor(donor);
            match.setDonorResponse(DonorResponse.PENDING);
            match.setDonorConsent(false);
            match.setAdminApproval(AdminApprovalStatus.PENDING);

            donorMatchRepository.save(match);

            // Notify the matched donor.
            Notification notification = new Notification();

            notification.setUser(donor.getUser());
            notification.setBloodRequest(request);
            notification.setMessage(
                    "A new " + request.getUrgency()
                    + " blood request matches your donor profile. "
                    + "Please review it in My Matched Requests."
            );

            notificationRepository.save(notification);
        }

        // Update matching status.
        request.setMatchingStatus(
                donorMatchRepository.findAllByBloodRequestId(requestId).isEmpty()
                        ? MatchingStatus.NO_MATCHES
                        : MatchingStatus.MATCHES_FOUND
        );

        requestRepository.save(request);
    }

    // ---------------- VIEW MATCHES ----------------

    @Transactional(readOnly = true)
    public List<DonorMatch> getMatches(
            Long requestId, String requesterEmail) {

        BloodRequest request = requestRepository
                .findByIdAndRequesterEmail(requestId, requesterEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Request not found"));

        return donorMatchRepository.findAllByBloodRequestId(request.getId());
    }

    // ---------------- REFRESH MATCHES ----------------

    @Transactional
    public void refreshMatches(Long requestId, String requesterEmail) {

        requestRepository.findByIdAndRequesterEmail(requestId, requesterEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Request not found"));

        // generateMatches() also checks urgency and admin approval.
        generateMatches(requestId);
    }

    // ---------------- APPROVED DONOR CONTACTS ----------------

    @Transactional(readOnly = true)
    public List<DonorMatch> getApprovedContacts(
            Long requestId, String requesterEmail) {

        BloodRequest request = requestRepository
                .findByIdAndRequesterEmail(requestId, requesterEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Request not found."));

        RequestAdminStatus status = request.getAdminStatus();

        // Never disclose contacts for inactive requests.
        if (status == RequestAdminStatus.CLOSED
                || status == RequestAdminStatus.REJECTED
                || status == RequestAdminStatus.FULFILLED) {

            return List.of();
        }

        return donorMatchRepository
                .findMatchesWithDonorDetails(requestId)
                .stream()
                .filter(match ->
                        match.getDonorResponse() == DonorResponse.ACCEPTED
                        && match.isDonorConsent()
                        && match.getAdminApproval()
                                == AdminApprovalStatus.APPROVED)
                .toList();
    }

    // ---------------- CONTACT ACCESS REVOCATION ----------------

    /**
     * Revokes all previously approved contact disclosures
     * associated with a blood request.
     *
     * Donor acceptance and consent records are preserved.
     * Original approval metadata is also retained.
     */
    @Transactional
    public void revokeApprovedContacts(Long requestId) {

        donorMatchRepository.revokeApprovedDisclosures(
                requestId,
                AdminApprovalStatus.APPROVED,
                AdminApprovalStatus.REVOKED
        );
    }
}
