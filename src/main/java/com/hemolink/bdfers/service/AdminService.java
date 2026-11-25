package com.hemolink.bdfers.service;

import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.*;
import com.hemolink.bdfers.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final DonorProfileRepository donorProfileRepository;
    private final DonorMatchRepository donorMatchRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final UserRepository userRepository;
    private final DonorMatchingService matchingService;

    public AdminService(
            DonorProfileRepository donorProfileRepository,
            DonorMatchRepository donorMatchRepository,
            BloodRequestRepository bloodRequestRepository,
            UserRepository userRepository,
            DonorMatchingService matchingService) {

        this.donorProfileRepository = donorProfileRepository;
        this.donorMatchRepository = donorMatchRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.userRepository = userRepository;
        this.matchingService = matchingService;
    }

    // ---------------- DONOR ELIGIBILITY ----------------

    @Transactional(readOnly = true)
    public List<DonorProfile> getAllDonors() {
        return donorProfileRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void reviewDonor(
            Long donorId,
            EligibilityStatus decision,
            String reason,
            String adminEmail) {

        if (decision != EligibilityStatus.ELIGIBLE
                && decision != EligibilityStatus.INELIGIBLE) {
            throw new IllegalArgumentException(
                    "Invalid eligibility decision.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "A review reason is required.");
        }

        DonorProfile donor = donorProfileRepository.findById(donorId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Donor profile not found."));

        AppUser admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Administrator not found."));

        donor.setEligibilityStatus(decision);
        donor.setEligibilityReason(reason.trim());
        donor.setEligibilityReviewedBy(admin);
        donor.setEligibilityReviewedAt(LocalDateTime.now());

        donorProfileRepository.save(donor);
    }

    // ---------------- BLOOD REQUEST REVIEW ----------------

    @Transactional(readOnly = true)
    public List<BloodRequest> getPendingBloodRequests() {
        return bloodRequestRepository
                .findAllByAdminStatusOrderByCreatedAtDesc(
                        RequestAdminStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<BloodRequest> getAllBloodRequests() {
        return bloodRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void reviewBloodRequest(
            Long requestId,
            RequestAdminStatus decision,
            String rejectionReason) {

        if (decision != RequestAdminStatus.APPROVED
                && decision != RequestAdminStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Invalid blood request decision.");
        }

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Blood request not found."));

        if (request.getAdminStatus() != RequestAdminStatus.PENDING) {
            throw new IllegalStateException(
                    "This blood request has already been reviewed.");
        }

        if (decision == RequestAdminStatus.REJECTED
                && (rejectionReason == null || rejectionReason.isBlank())) {
            throw new IllegalArgumentException(
                    "Please provide a reason for rejecting the request.");
        }

        if (decision == RequestAdminStatus.REJECTED) {

            request.setAdminStatus(RequestAdminStatus.REJECTED);
            request.setRejectionReason(rejectionReason.trim());

            bloodRequestRepository.save(request);

            // Revoke any previously approved contact disclosures.
            matchingService.revokeApprovedContacts(requestId);

            return;
        }

        // Approve the request.
        request.setAdminStatus(RequestAdminStatus.APPROVED);
        request.setRejectionReason(null);

        bloodRequestRepository.save(request);

        // Critical requests were already matched on submission.
        // generateMatches() avoids duplicate donor matches.
        if (request.getUrgency() == RequestUrgency.NORMAL) {
            matchingService.generateMatches(requestId);
        }
    }

    // ---------------- CONTACT DISCLOSURE REVIEW ----------------

    @Transactional(readOnly = true)
    public List<DonorMatch> getPendingDisclosureRequests() {
        return donorMatchRepository
                .findAllByDonorResponseAndDonorConsentTrueAndAdminApprovalOrderByConsentAtDesc(
                        DonorResponse.ACCEPTED,
                        AdminApprovalStatus.PENDING);
    }

    @Transactional
    public void reviewContactDisclosure(
            Long matchId,
            AdminApprovalStatus decision,
            String adminEmail) {

        if (decision != AdminApprovalStatus.APPROVED
                && decision != AdminApprovalStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Invalid contact disclosure decision.");
        }

        DonorMatch match = donorMatchRepository.findMatchWithDetails(matchId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Disclosure request not found."));

        if (match.getDonorResponse() != DonorResponse.ACCEPTED
                || !match.isDonorConsent()) {
            throw new IllegalStateException(
                    "Donor acceptance and contact consent are required.");
        }

        RequestAdminStatus status =
                match.getBloodRequest().getAdminStatus();

        if (status == RequestAdminStatus.CLOSED
                || status == RequestAdminStatus.REJECTED
                || status == RequestAdminStatus.FULFILLED) {
            throw new IllegalStateException(
                    "Contact disclosure is not allowed for an inactive request.");
        }

        if (match.getAdminApproval() != AdminApprovalStatus.PENDING) {
            throw new IllegalStateException(
                    "This disclosure request has already been reviewed.");
        }

        AppUser admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Administrator not found."));

        match.setAdminApproval(decision);
        match.setApprovedBy(admin);
        match.setApprovalAt(LocalDateTime.now());

        donorMatchRepository.save(match);
    }
}