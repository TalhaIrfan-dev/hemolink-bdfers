
package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.request.BloodRequestRequest;
import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.enums.MatchingStatus;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.repository.BloodRequestRepository;
import com.hemolink.bdfers.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final UserRepository userRepository;
    private final DonorMatchingService matchingService;

    public BloodRequestService(
            BloodRequestRepository bloodRequestRepository,
            UserRepository userRepository,
            DonorMatchingService matchingService) {

        this.bloodRequestRepository = bloodRequestRepository;
        this.userRepository = userRepository;
        this.matchingService = matchingService;
    }

    @Transactional
    public BloodRequest createRequest(
            BloodRequestRequest request, String email) {

        AppUser requester = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        BloodRequest bloodRequest = new BloodRequest();

        bloodRequest.setRequester(requester);
        bloodRequest.setBloodGroup(request.getBloodGroup());
        bloodRequest.setUnitsRequired(request.getUnitsRequired());
        bloodRequest.setFulfilledUnits(0);
        bloodRequest.setCity(request.getCity().trim());
        bloodRequest.setHospitalName(request.getHospitalName().trim());
        bloodRequest.setRequiredAt(request.getRequiredAt());
        bloodRequest.setUrgency(request.getUrgency());

        bloodRequest.setAdminStatus(RequestAdminStatus.PENDING);
        bloodRequest.setMatchingStatus(MatchingStatus.NOT_STARTED);

        BloodRequest savedRequest =
                bloodRequestRepository.saveAndFlush(bloodRequest);

        // Critical requests start donor matching immediately.
        if (request.getUrgency() == RequestUrgency.CRITICAL) {
            matchingService.generateMatches(savedRequest.getId());
        }

        return savedRequest;
    }

    @Transactional(readOnly = true)
    public List<BloodRequest> getMyRequests(String email) {
        return bloodRequestRepository
                .findAllByRequesterEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public void closeRequest(Long requestId, String email) {

        BloodRequest request = bloodRequestRepository
                .findByIdAndRequesterEmail(requestId, email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Request not found"));

        if (request.getAdminStatus() != RequestAdminStatus.PENDING
                && request.getAdminStatus() != RequestAdminStatus.APPROVED) {
            throw new IllegalStateException(
                    "This request cannot be closed in its current status");
        }

        request.setAdminStatus(RequestAdminStatus.CLOSED);
        request.setMatchingStatus(MatchingStatus.NOT_STARTED);

        bloodRequestRepository.save(request);
    }
}
