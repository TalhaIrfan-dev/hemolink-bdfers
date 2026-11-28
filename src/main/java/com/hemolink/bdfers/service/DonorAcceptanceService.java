package com.hemolink.bdfers.service;

import com.hemolink.bdfers.enums.NotificationType;

import com.hemolink.bdfers.dto.request.HealthCheckRequest;
import com.hemolink.bdfers.dto.response.AcceptContext;
import com.hemolink.bdfers.dto.response.Advice;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonorHealthCheck;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.entity.Notification;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.exception.NotFoundException;
import com.hemolink.bdfers.repository.DonorHealthCheckRepository;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Donor acceptance with an advisory health check (FR-14 as changed by D-01).
 *
 * <ol>
 *   <li>The donor enters haemoglobin, CBC date, weight and units offered.</li>
 *   <li>{@link EligibilityAdvisor} returns "Looks OK" or "Not advised"
 *       with reasons.</li>
 *   <li>The donor accepts (confirming they understand when not advised)
 *       or declines. The advice never blocks the donor.</li>
 * </ol>
 *
 * The health check, the advice, the acceptance and the profile weight are
 * saved in one transaction. Health data is never shown to requesters.
 */
@Service
public class DonorAcceptanceService {

    private final DonorMatchRepository matchRepository;
    private final DonorHealthCheckRepository healthCheckRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final NotificationService notificationService;
    private final GuidanceSettingsService settingsService;
    private final EligibilityAdvisor advisor;

    public DonorAcceptanceService(
            DonorMatchRepository matchRepository,
            DonorHealthCheckRepository healthCheckRepository,
            DonorProfileRepository donorProfileRepository,
            NotificationService notificationService,
            GuidanceSettingsService settingsService,
            EligibilityAdvisor advisor) {

        this.matchRepository = matchRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.notificationService = notificationService;
        this.settingsService = settingsService;
        this.advisor = advisor;
    }

    /** Request summary for the health-check page. */
    @Transactional(readOnly = true)
    public AcceptContext getContext(Long matchId, String donorEmail) {
        return toContext(loadAcceptableMatch(matchId, donorEmail));
    }

    /** Empty form with the donor's current weight and 1 unit pre-filled. */
    @Transactional(readOnly = true)
    public HealthCheckRequest prefilledForm(Long matchId, String donorEmail) {
        DonorMatch match = loadAcceptableMatch(matchId, donorEmail);

        HealthCheckRequest form = new HealthCheckRequest();
        form.setWeightKg(match.getDonor().getWeight());
        form.setUnitsOffered(1);
        return form;
    }

    /** Calculates the advice without saving anything. */
    @Transactional(readOnly = true)
    public Advice preview(Long matchId, String donorEmail, HealthCheckRequest form) {
        DonorMatch match = loadAcceptableMatch(matchId, donorEmail);
        validateUnits(match.getBloodRequest(), form.getUnitsOffered());
        return assess(match.getDonor(), form);
    }

    /**
     * Saves the health check and records the donor's acceptance.
     * The advice is recalculated on the server; if it is "Not advised" the
     * donor must have confirmed that they understand it.
     */
    @Transactional
    public Advice accept(Long matchId, String donorEmail, HealthCheckRequest form) {

        DonorMatch match = loadAcceptableMatch(matchId, donorEmail);
        BloodRequest request = match.getBloodRequest();

        validateUnits(request, form.getUnitsOffered());

        if (healthCheckRepository.findByMatch_Id(matchId).isPresent()) {
            throw new BusinessRuleException(
                    "You have already responded to this request.");
        }

        Advice advice = assess(match.getDonor(), form);

        if (!advice.isOk() && !form.isAcknowledged()) {
            throw new BusinessRuleException(
                    "Please confirm that you understand the advice before accepting.");
        }

        // 1. Health check (advisory record; never shown to requesters).
        DonorHealthCheck healthCheck = new DonorHealthCheck();
        healthCheck.setMatch(match);
        healthCheck.setHaemoglobinGdl(form.getHaemoglobinGdl());
        healthCheck.setCbcDate(form.getCbcDate());
        healthCheck.setWeightKg(form.getWeightKg());
        healthCheck.setUnitsOffered(form.getUnitsOffered());
        healthCheck.setAdvice(advice.getLevel());
        healthCheck.setAdviceReasons(advice.reasonsAsText());
        healthCheck.setAcceptedAgainstAdvice(!advice.isOk());
        healthCheckRepository.save(healthCheck);

        // 2. Keep the donor profile weight current.
        DonorProfile donor = match.getDonor();
        donor.setWeight(form.getWeightKg());
        donorProfileRepository.save(donor);

        // 3. Record the acceptance. Contact consent is a separate step (D-16).
        match.setDonorResponse(DonorResponse.ACCEPTED);
        match.setDonorConsent(false);
        match.setConsentAt(null);
        matchRepository.save(match);

        // 4. Notify the requester (no health information included).
        Notification notification = new Notification();
        notification.setUser(request.getRequester());
        notification.setBloodRequest(request);
        notification.setType(NotificationType.RESPONSE);
        notification.setMessage(
                "A matched donor has accepted your blood request "
                + String.format("REQ-%04d", request.getId())
                + ". Their contact details stay hidden until they choose to share them.");
        notificationService.saveAll(java.util.List.of(notification));

        return advice;
    }

    // ---------------- HELPERS ----------------

    /**
     * Loads the donor's own match and checks that it can still be accepted:
     * no earlier response, active donor profile, and a request that accepts
     * donor actions (D-15).
     */
    private DonorMatch loadAcceptableMatch(Long matchId, String donorEmail) {

        DonorMatch match = matchRepository
                .findByIdAndDonorUserEmail(matchId, donorEmail)
                .orElseThrow(() ->
                        new NotFoundException("Matched request not found."));

        if (match.getDonorResponse() != DonorResponse.PENDING) {
            throw new BusinessRuleException(
                    "You have already responded to this request.");
        }

        if (!match.getDonor().isActive()) {
            throw new BusinessRuleException(
                    "Your donor profile has been deactivated by an administrator, "
                    + "so you cannot respond to requests.");
        }

        RequestStatusPolicy.requireDonorActionsAllowed(
                match.getBloodRequest().getAdminStatus(),
                match.getBloodRequest().getUrgency());

        return match;
    }

    /** Units offered may not exceed the units the request still needs. */
    private static void validateUnits(BloodRequest request, Integer unitsOffered) {
        int remaining = remainingUnits(request);

        if (unitsOffered != null && unitsOffered > remaining) {
            throw new BusinessRuleException(
                    "This request needs only " + remaining + " more unit"
                    + (remaining == 1 ? "" : "s") + ". Please offer "
                    + remaining + " or fewer.");
        }
    }

    private static int remainingUnits(BloodRequest request) {
        int fulfilled = request.getFulfilledUnits() == null ? 0 : request.getFulfilledUnits();
        return Math.max(0, request.getUnitsRequired() - fulfilled);
    }

    private Advice assess(DonorProfile donor, HealthCheckRequest form) {
        return advisor.assess(
                new EligibilityAdvisor.Input(
                        donor.getDateOfBirth(),
                        donor.getLastDonationDate(),
                        form.getWeightKg(),
                        form.getHaemoglobinGdl(),
                        form.getCbcDate(),
                        form.getUnitsOffered()),
                settingsService.current());
    }

    private static AcceptContext toContext(DonorMatch match) {
        BloodRequest request = match.getBloodRequest();
        return new AcceptContext(
                match.getId(),
                request.getId(),
                request.getBloodGroup().getLabel(),
                request.getCity(),
                request.getHospitalName(),
                request.getUrgency().getLabel(),
                remainingUnits(request));
    }
}
