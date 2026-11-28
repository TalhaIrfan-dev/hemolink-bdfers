package com.hemolink.bdfers.service;

import com.hemolink.bdfers.exception.NotFoundException;

import com.hemolink.bdfers.dto.request.DonorProfileRequest;
import com.hemolink.bdfers.dto.response.Advice;
import com.hemolink.bdfers.dto.response.DeactivationNotice;
import com.hemolink.bdfers.enums.DonorStatusAction;
import com.hemolink.bdfers.repository.DonorStatusChangeRepository;
import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.AvailabilityStatus;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import com.hemolink.bdfers.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DonorProfileService {

    private final DonorProfileRepository donorRepository;
    private final UserRepository userRepository;
    private final EligibilityAdvisor advisor;
    private final GuidanceSettingsService settingsService;
    private final DonorStatusChangeRepository statusChangeRepository;

    public DonorProfileService(
            DonorProfileRepository donorRepository,
            UserRepository userRepository,
            EligibilityAdvisor advisor,
            GuidanceSettingsService settingsService,
            DonorStatusChangeRepository statusChangeRepository) {
        this.donorRepository = donorRepository;
        this.userRepository = userRepository;
        this.advisor = advisor;
        this.settingsService = settingsService;
        this.statusChangeRepository = statusChangeRepository;
    }

    /**
     * Notice for a donor whose profile was deactivated, with the most
     * recent reason. Null if the profile is active or does not exist.
     */
    @Transactional(readOnly = true)
    public DeactivationNotice getDeactivationNotice(String email) {
        return donorRepository.findByUserEmail(email)
                .filter(profile -> !profile.isActive())
                .map(profile -> statusChangeRepository
                        .findFirstByDonor_IdAndActionOrderByIdDesc(
                                profile.getId(), DonorStatusAction.DEACTIVATED)
                        .map(change -> new DeactivationNotice(
                                change.getReason(), change.getChangedAt()))
                        .orElseGet(() -> new DeactivationNotice(null, null)))
                .orElse(null);
    }

    /**
     * Advisory guidance based on the profile alone (age, weight, time
     * since last donation). Haemoglobin is checked when accepting a
     * request. Returns null if the user has no donor profile (D-01).
     */
    @Transactional(readOnly = true)
    public Advice getProfileAdvice(String email) {
        return donorRepository.findByUserEmail(email)
                .map(profile -> advisor.assess(
                        new EligibilityAdvisor.Input(
                                profile.getDateOfBirth(),
                                profile.getLastDonationDate(),
                                profile.getWeight(),
                                null,
                                null,
                                null),
                        settingsService.current()))
                .orElse(null);
    }

    public DonorProfile getProfile(String email) {
        return donorRepository.findByUserEmail(email)
                .orElse(null);
    }

    @Transactional
    public void saveProfile(String email, DonorProfileRequest request) {

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User account not found."));

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

        // Eligibility is advisory (D-01) and calculated when needed, so
        // saving the profile no longer resets anything (DEF-02).

        donorRepository.save(profile);
    }

    /**
     * Quick availability switch from the dashboard (FR-05). Changes only
     * availability; nothing else on the profile is touched.
     */
    @Transactional
    public void setAvailability(String email, AvailabilityStatus availability) {
        if (availability == null) {
            throw new IllegalArgumentException("Please choose Available or Unavailable.");
        }
        DonorProfile profile = donorRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        "Create your donor profile before setting availability."));
        profile.setAvailability(availability);
        donorRepository.save(profile);
    }
}
