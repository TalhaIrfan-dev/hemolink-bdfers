package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.response.Advice;
import com.hemolink.bdfers.dto.response.DonorDetailView;
import com.hemolink.bdfers.dto.response.DonorMatchHistoryRow;
import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonationRecord;
import com.hemolink.bdfers.entity.DonorHealthCheck;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.entity.DonorStatusChange;
import com.hemolink.bdfers.enums.AvailabilityStatus;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.enums.DonationOutcome;
import com.hemolink.bdfers.enums.DonorStatusAction;
import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.exception.NotFoundException;
import com.hemolink.bdfers.repository.DonationRecordRepository;
import com.hemolink.bdfers.repository.DonorHealthCheckRepository;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import com.hemolink.bdfers.repository.DonorStatusChangeRepository;
import com.hemolink.bdfers.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Donor Directory (D-05) and donor deactivation/reactivation (FR-17).
 *
 * Effects of deactivation (owner decisions, Module 2.5):
 * <ul>
 *   <li>excluded from matching and cannot respond (already enforced);</li>
 *   <li>contact details already shared are hidden from requesters;</li>
 *   <li>pending matches stay on record but show "No longer available";</li>
 *   <li>the donor sees a notice with the reason.</li>
 * </ul>
 * Reactivation reverses all of these.
 */
@Service
public class DonorAdminService {

    public static final int PAGE_SIZE = 20;

    private static final int MAX_REASON_LENGTH = 255;

    private final DonorProfileRepository donorProfileRepository;
    private final DonorMatchRepository donorMatchRepository;
    private final DonorHealthCheckRepository healthCheckRepository;
    private final DonationRecordRepository donationRecordRepository;
    private final DonorStatusChangeRepository statusChangeRepository;
    private final UserRepository userRepository;
    private final EligibilityAdvisor advisor;
    private final GuidanceSettingsService settingsService;
    private final NotificationService notificationService;

    public DonorAdminService(
            DonorProfileRepository donorProfileRepository,
            DonorMatchRepository donorMatchRepository,
            DonorHealthCheckRepository healthCheckRepository,
            DonationRecordRepository donationRecordRepository,
            DonorStatusChangeRepository statusChangeRepository,
            UserRepository userRepository,
            EligibilityAdvisor advisor,
            GuidanceSettingsService settingsService,
            NotificationService notificationService) {

        this.donorProfileRepository = donorProfileRepository;
        this.donorMatchRepository = donorMatchRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.donationRecordRepository = donationRecordRepository;
        this.statusChangeRepository = statusChangeRepository;
        this.userRepository = userRepository;
        this.advisor = advisor;
        this.settingsService = settingsService;
        this.notificationService = notificationService;
    }

    // ---------------- DIRECTORY ----------------

    @Transactional(readOnly = true)
    public Page<DonorProfile> search(
            String query,
            BloodGroup bloodGroup,
            String city,
            AvailabilityStatus availability,
            Boolean active,
            int page) {

        return donorProfileRepository.searchDirectory(
                blankToNull(query),
                bloodGroup,
                blankToNull(city),
                availability,
                active,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE));
    }

    @Transactional(readOnly = true)
    public List<String> getDonorCities() {
        return donorProfileRepository.findDistinctCities();
    }

    // ---------------- DONOR DETAIL ----------------

    @Transactional(readOnly = true)
    public DonorDetailView getDetail(Long donorId) {

        DonorProfile profile = findDonor(donorId);
        profile.getUser().getFullName(); // load the account for the page

        Advice advice = advisor.assess(
                new EligibilityAdvisor.Input(
                        profile.getDateOfBirth(),
                        profile.getLastDonationDate(),
                        profile.getWeight(),
                        null, null, null),
                settingsService.current());

        Map<Long, DonorHealthCheck> healthChecks = new HashMap<>();
        healthCheckRepository.findAllByDonorId(donorId)
                .forEach(hc -> healthChecks.put(hc.getMatch().getId(), hc));

        Map<Long, DonationRecord> donations = new HashMap<>();
        donationRecordRepository.findAllByDonorId(donorId)
                .forEach(dr -> donations.put(dr.getMatch().getId(), dr));

        List<DonorMatchHistoryRow> history = donorMatchRepository
                .findAllWithRequestByDonorId(donorId)
                .stream()
                .map(match -> {
                    BloodRequest request = match.getBloodRequest();
                    return new DonorMatchHistoryRow(
                            request.getId(),
                            request.getBloodGroup().getLabel(),
                            request.getHospitalName(),
                            request.getCity(),
                            request.getUrgency().getLabel(),
                            request.getAdminStatus().getLabel(),
                            match.getDonorResponse().getLabel(),
                            match.isDonorConsent(),
                            match.getConsentAt(),
                            healthChecks.get(match.getId()),
                            donations.get(match.getId()));
                })
                .toList();

        long completed = donations.values().stream()
                .filter(dr -> dr.getOutcome() == DonationOutcome.COMPLETED)
                .count();

        return new DonorDetailView(
                profile,
                advice,
                history,
                statusChangeRepository.findAllByDonor_IdOrderByIdDesc(donorId),
                completed);
    }

    // ---------------- DEACTIVATE / REACTIVATE (FR-17) ----------------

    @Transactional
    public void deactivate(Long donorId, String reason, String adminEmail) {
        changeStatus(donorId, DonorStatusAction.DEACTIVATED, reason, adminEmail);
    }

    @Transactional
    public void reactivate(Long donorId, String reason, String adminEmail) {
        changeStatus(donorId, DonorStatusAction.REACTIVATED, reason, adminEmail);
    }

    private void changeStatus(
            Long donorId, DonorStatusAction action, String reason, String adminEmail) {

        DonorProfile donor = findDonor(donorId);
        boolean deactivating = action == DonorStatusAction.DEACTIVATED;

        if (deactivating && !donor.isActive()) {
            throw new BusinessRuleException("This donor profile is already deactivated.");
        }
        if (!deactivating && donor.isActive()) {
            throw new BusinessRuleException("This donor profile is already active.");
        }

        String cleaned = reason == null ? "" : reason.trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(
                    deactivating
                            ? "Please give a reason for deactivating this donor."
                            : "Please give a reason for reactivating this donor.");
        }
        if (cleaned.length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException(
                    "The reason cannot exceed " + MAX_REASON_LENGTH + " characters.");
        }

        AppUser admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new NotFoundException("Administrator not found."));

        donor.setActive(!deactivating);
        donorProfileRepository.save(donor);

        DonorStatusChange change = new DonorStatusChange();
        change.setDonor(donor);
        change.setAction(action);
        change.setReason(cleaned);
        change.setChangedBy(admin);
        statusChangeRepository.save(change);

        // FR-13: tell the donor; on deactivation also tell requesters whose
        // shared contact details from this donor are now hidden.
        if (deactivating) {
            notificationService.donorDeactivated(donor, cleaned);
        } else {
            notificationService.donorReactivated(donor);
        }
    }

    // ---------------- HELPERS ----------------

    private DonorProfile findDonor(Long donorId) {
        return donorProfileRepository.findById(donorId)
                .orElseThrow(() -> new NotFoundException("Donor profile not found."));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
