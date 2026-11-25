package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.request.DonorProfileRequest;
import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.EligibilityStatus;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import com.hemolink.bdfers.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DonorProfileService {

    private final DonorProfileRepository donorRepository;
    private final UserRepository userRepository;

    public DonorProfileService(
            DonorProfileRepository donorRepository,
            UserRepository userRepository) {
        this.donorRepository = donorRepository;
        this.userRepository = userRepository;
    }

    public DonorProfile getProfile(String email) {
        return donorRepository.findByUserEmail(email)
                .orElse(null);
    }

    @Transactional
    public void saveProfile(String email, DonorProfileRequest request) {

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getLastDonationDate() != null
                && request.getLastDonationDate().isBefore(request.getDateOfBirth())) {
            throw new IllegalArgumentException(
                    "Last donation date cannot be before date of birth.");
        }

        DonorProfile profile = donorRepository.findByUserEmail(email)
                .orElseGet(DonorProfile::new);

        profile.setUser(user);
        profile.setBloodGroup(request.getBloodGroup());
        profile.setCity(request.getCity().trim());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setWeight(request.getWeight());
        profile.setLastDonationDate(request.getLastDonationDate());
        profile.setAvailability(request.getAvailability());

        // Eligibility thresholds must be verified before assessment.
        profile.setEligibilityStatus(EligibilityStatus.REQUIRES_REVIEW);
        profile.setEligibilityReason("Eligibility criteria require review.");

        donorRepository.save(profile);
    }
}