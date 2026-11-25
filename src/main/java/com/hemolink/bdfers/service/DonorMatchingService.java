
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

    @Transactional
    public void generateMatches(Long requestId) {

        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Blood request not found"));

        if (request.getAdminStatus() == RequestAdminStatus.CLOSED
                || request.getAdminStatus() == RequestAdminStatus.REJECTED
                || request.getAdminStatus() == RequestAdminStatus.FULFILLED) {
            throw new IllegalStateException(
                    "Matching is not allowed for this request.");
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

            if (donor.getUser().getId()
                    .equals(request.getRequester().getId())) {
                continue;
            }

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

            Notification notification = new Notification();
            notification.setUser(donor.getUser());
            notification.setBloodRequest(request);
            notification.setMessage(
                    "A new " + request.getUrgency()
                    + " blood request matches your donor profile. "
                    + "Please review it in My Matched Requests.");

            notificationRepository.save(notification);
        }

        request.setMatchingStatus(
                donorMatchRepository.findAllByBloodRequestId(requestId).isEmpty()
                        ? MatchingStatus.NO_MATCHES
                        : MatchingStatus.MATCHES_FOUND
        );

        requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<DonorMatch> getMatches(
            Long requestId, String requesterEmail) {

        BloodRequest request = requestRepository
                .findByIdAndRequesterEmail(requestId, requesterEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Request not found"));

        return donorMatchRepository.findAllByBloodRequestId(request.getId());
    }

    @Transactional
    public void refreshMatches(Long requestId, String requesterEmail) {

        requestRepository.findByIdAndRequesterEmail(requestId, requesterEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Request not found"));

        generateMatches(requestId);
    }
}
