
package com.hemolink.bdfers.service;

import com.hemolink.bdfers.enums.NotificationType;

import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.exception.NotFoundException;

import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonationRecord;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.DonationOutcome;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.repository.BloodRequestRepository;
import com.hemolink.bdfers.repository.DonationRecordRepository;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class DonationReportingService {

    private final DonorMatchRepository donorMatchRepository;
    private final DonationRecordRepository donationRecordRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final NotificationService notificationService;

    public DonationReportingService(
            DonorMatchRepository donorMatchRepository,
            DonationRecordRepository donationRecordRepository,
            BloodRequestRepository bloodRequestRepository,
            DonorProfileRepository donorProfileRepository,
            NotificationService notificationService) {

        this.donorMatchRepository = donorMatchRepository;
        this.donationRecordRepository = donationRecordRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void reportOutcome(
            Long matchId,
            String donorEmail,
            DonationOutcome outcome,
            Integer unitsDonated,
            LocalDate donationDate) {

        if (outcome == null) {
            throw new IllegalArgumentException(
                    "Please select a donation outcome.");
        }

        // Ensure that the match belongs to the logged-in donor.
        DonorMatch match = donorMatchRepository
                .findByIdAndDonorUserEmail(matchId, donorEmail)
                .orElseThrow(() -> new NotFoundException(
                        "Matched request not found."));

        if (match.getDonorResponse() != DonorResponse.ACCEPTED) {
            throw new BusinessRuleException(
                    "You must accept the request before reporting its outcome.");
        }

        // Lock the request row while updating fulfilled units.
        Long requestId = match.getBloodRequest().getId();

        BloodRequest request = bloodRequestRepository
                .findByIdForUpdate(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "Blood request not found."));

        validateActiveRequest(request);

        // A donor can submit only one outcome for a match.
        if (donationRecordRepository.existsByMatch_Id(matchId)) {
            throw new BusinessRuleException(
                    "You have already submitted the outcome for this request.");
        }

        if (outcome == DonationOutcome.COMPLETED) {
            validateCompletedDonation(
                    request, unitsDonated, donationDate);

            int currentFulfilled = request.getFulfilledUnits() == null
                    ? 0
                    : request.getFulfilledUnits();

            int newFulfilled = currentFulfilled + unitsDonated;

            if (newFulfilled > request.getUnitsRequired()) {
                throw new IllegalArgumentException(
                        "The reported units exceed the remaining required units.");
            }

            request.setFulfilledUnits(newFulfilled);

            // Update the donor's last donation date.
            DonorProfile donor = match.getDonor();

            if (donor.getLastDonationDate() == null
                    || donationDate.isAfter(donor.getLastDonationDate())) {
                donor.setLastDonationDate(donationDate);
            }

            // Eligibility advice is recalculated from the new last donation
            // date whenever it is shown; nothing is blocked (D-01, FR-18).

            donorProfileRepository.save(donor);

            // Mark the request fulfilled once all required units are reported.
            // Critical requests may go straight from Pending to Fulfilled
            // because admin review is optional for them (D-15).
            // Contact details stop being visible automatically, because
            // they are only shown for active requests (D-16).
            if (newFulfilled >= request.getUnitsRequired()) {
                RequestStatusPolicy.requireTransition(
                        request.getAdminStatus(),
                        RequestAdminStatus.FULFILLED,
                        request.getUrgency());

                request.setAdminStatus(RequestAdminStatus.FULFILLED);
            }

        } else {
            // NOT_OCCURRED must not claim any donated units or date.
            if (unitsDonated != null && unitsDonated != 0) {
                throw new IllegalArgumentException(
                        "Units donated must be zero when the donation did not occur.");
            }

            if (donationDate != null) {
                throw new IllegalArgumentException(
                        "A donation date must not be provided when the donation did not occur.");
            }

            unitsDonated = 0;
        }

        DonationRecord record = new DonationRecord();
        record.setMatch(match);
        record.setOutcome(outcome);
        record.setUnitsDonated(unitsDonated);
        record.setDonationDate(donationDate);

        donationRecordRepository.save(record);
        bloodRequestRepository.save(request);

        // Notify the requester about the reported outcome (FR-13).
        String requestLabel = NotificationService.id(request);
        String message;

        if (outcome == DonationOutcome.COMPLETED) {
            message = request.getAdminStatus() == RequestAdminStatus.FULFILLED
                    ? "Your blood request " + requestLabel
                      + " has been fulfilled: all required units have been reported as donated."
                    : "A donor has reported a completed donation of "
                      + unitsDonated + " unit(s) for your blood request " + requestLabel + ".";
        } else {
            message = "A donor has reported that the donation did not take place for your blood request "
                    + requestLabel + ". The request stays open for other donors.";
        }

        notificationService.notify(
                request.getRequester(), request, NotificationType.DONATION, message);

        // Tell other matched donors that the request no longer needs them.
        if (request.getAdminStatus() == RequestAdminStatus.FULFILLED) {
            notificationService.requestEndedForDonors(
                    request, "has been fulfilled.", matchId);
        }
    }

    @Transactional(readOnly = true)
    public Set<Long> getReportedMatchIds(String donorEmail) {

        return new HashSet<>(
                donationRecordRepository
                        .findReportedMatchIdsByDonorEmail(donorEmail));
    }

    /** Donors may report only while the request accepts donor actions (D-15). */
    private void validateActiveRequest(BloodRequest request) {
        RequestStatusPolicy.requireDonorActionsAllowed(
                request.getAdminStatus(),
                request.getUrgency());
    }

    private void validateCompletedDonation(
            BloodRequest request,
            Integer unitsDonated,
            LocalDate donationDate) {

        if (unitsDonated == null || unitsDonated <= 0) {
            throw new IllegalArgumentException(
                    "Units donated must be greater than zero.");
        }

        if (donationDate == null) {
            throw new IllegalArgumentException(
                    "Please provide the donation date.");
        }

        if (donationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Donation date cannot be in the future.");
        }

        int fulfilled = request.getFulfilledUnits() == null
                ? 0
                : request.getFulfilledUnits();

        int remainingUnits = request.getUnitsRequired() - fulfilled;

        if (unitsDonated > remainingUnits) {
            throw new IllegalArgumentException(
                    "You can report a maximum of "
                    + remainingUnits + " remaining unit(s).");
        }
    }
}
