
package com.hemolink.bdfers.service;

import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.EligibilityStatus;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import com.hemolink.bdfers.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final DonorProfileRepository donorProfileRepository;
    private final UserRepository userRepository;

    public AdminService(
            DonorProfileRepository donorProfileRepository,
            UserRepository userRepository) {
        this.donorProfileRepository = donorProfileRepository;
        this.userRepository = userRepository;
    }

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
            throw new IllegalArgumentException("Invalid eligibility decision.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A review reason is required.");
        }

        DonorProfile donor = donorProfileRepository.findById(donorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Donor profile not found."));

        AppUser admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Administrator not found."));

        donor.setEligibilityStatus(decision);
        donor.setEligibilityReason(reason.trim());
        donor.setEligibilityReviewedBy(admin);
        donor.setEligibilityReviewedAt(LocalDateTime.now());

        donorProfileRepository.save(donor);
    }
}
