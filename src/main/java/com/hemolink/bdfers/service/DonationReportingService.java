
package com.hemolink.bdfers.service;

import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonationRecord;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.entity.Notification;
import com.hemolink.bdfers.enums.DonationOutcome;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.EligibilityStatus;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.repository.BloodRequestRepository;
import com.hemolink.bdfers.repository.DonationRecordRepository;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import com.hemolink.bdfers.repository.NotificationRepository;
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
    private final NotificationRepository notificationRepository;
    private final DonorMatchingService matchingService;

    public DonationReportingService(
            DonorMatchRepository donorMatchRepository,
            DonationRecordRepository donationRecordRepository,
            BloodRequestRepository bloodRequestRepository,
            DonorProfileRepository donorProfileRepository,
            NotificationRepository notificationRepository,
            DonorMatchingService matchingService) {

        this.donorMatchRepository = donorMatchRepository;
        this.donationRecordRepository = donationRecordRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.notificationRepository = notificationRepository;
        this.matchingService = matchingService;
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
                .orElseThrow(() -> new IllegalArgumentException(
                        "Matched request not found."));

        if (match.getDonorResponse() != DonorResponse.ACCEPTED) {
            throw new IllegalStateException(
                    "You must accept the request before reporting its outcome.");
        }

        // Lock the request row while updating fulfilled units.
        Long requestId = match.getBloodRequest().getId();

        BloodRequest request = bloodRequestRepository
                .findByIdForUpdate(requestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Blood request not found."));

        validateActiveRequest(request);

        // A donor can submit only one outcome for a match.
        if (donationRecordRepository.existsByMatch_Id(matchId)) {
            throw new IllegalStateException(
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

            // A new donation report requires administrative eligibility review.
            donor.setEligibilityStatus(EligibilityStatus.REQUIRES_REVIEW);
            donor.setEligibilityReason(
                    "A donation has been reported. Eligibility requires administrative review.");
            donor.setEligibilityReviewedBy(null);
            donor.setEligibilityReviewedAt(null);

            donorProfileRepository.save(donor);

            // Mark request fulfilled once all required units are reported.
            if (newFulfilled >= request.getUnitsRequired()) {
                request.setAdminStatus(RequestAdminStatus.FULFILLED);

                // Revoke any previously approved contact disclosures.
                matchingService.revokeApprovedContacts(requestId);
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

        // Notify the requester about the reported outcome.
        Notification notification = new Notification();
        notification.setUser(request.getRequester());
        notification.setBloodRequest(request);

        if (outcome == DonationOutcome.COMPLETED) {
            notification.setMessage(
                    "A donor has reported a completed donation of "
                    + unitsDonated + " unit(s) for your blood request #"
                    + requestId + ".");

            if (request.getAdminStatus() == RequestAdminStatus.FULFILLED) {
                notification.setMessage(
                        "Your blood request #" + requestId
                        + " has been marked fulfilled based on reported donations.");
            }

        } else {
            notification.setMessage(
                    "A donor has reported that the donation did not occur "
                    + "for your blood request #" + requestId + ".");
        }

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Set<Long> getReportedMatchIds(String donorEmail) {

        return new HashSet<>(
                donationRecordRepository
                        .findReportedMatchIdsByDonorEmail(donorEmail));
    }

    private void validateActiveRequest(BloodRequest request) {

        RequestAdminStatus status = request.getAdminStatus();

        if (status == RequestAdminStatus.CLOSED
                || status == RequestAdminStatus.REJECTED
                || status == RequestAdminStatus.FULFILLED) {

            throw new IllegalStateException(
                    "This blood request is no longer active.");
        }
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
