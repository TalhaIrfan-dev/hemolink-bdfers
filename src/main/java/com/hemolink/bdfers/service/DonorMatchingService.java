
package com.hemolink.bdfers.service;

import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.*;
import com.hemolink.bdfers.repository.BloodRequestRepository;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DonorMatchingService {

    private final BloodRequestRepository requestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final DonorMatchRepository donorMatchRepository;

    public DonorMatchingService(
            BloodRequestRepository requestRepository,
            DonorProfileRepository donorProfileRepository,
            DonorMatchRepository donorMatchRepository) {
        this.requestRepository = requestRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.donorMatchRepository = donorMatchRepository;
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
                    "Matching is not allowed for this request");
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

            // A requester should not be matched to their own donor profile.
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
        }

        request.setMatchingStatus(
                donorMatchRepository.findAllByBloodRequestId(requestId).isEmpty()
                        ? MatchingStatus.NO_MATCHES
                        : MatchingStatus.MATCHES_FOUND
        );

        requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<DonorMatch> getMatches(Long requestId, String requesterEmail) {

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
