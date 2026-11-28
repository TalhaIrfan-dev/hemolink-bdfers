package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.request.BloodRequestRequest;
import com.hemolink.bdfers.dto.response.MyRequestsView;
import com.hemolink.bdfers.dto.response.RequestDetailView;
import com.hemolink.bdfers.dto.response.TimelineEntry;
import com.hemolink.bdfers.dto.response.TimelineEntry.State;
import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonationRecord;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.enums.DonationOutcome;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.MatchingStatus;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.enums.RequestListTab;
import com.hemolink.bdfers.enums.RequestStage;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.exception.NotFoundException;
import com.hemolink.bdfers.repository.BloodRequestRepository;
import com.hemolink.bdfers.repository.DonationRecordRepository;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BloodRequestService {

    private static final int MAX_REASON_LENGTH = 255;

    private final BloodRequestRepository bloodRequestRepository;
    private final UserRepository userRepository;
    private final DonorMatchingService matchingService;
    private final DonorMatchRepository donorMatchRepository;
    private final DonationRecordRepository donationRecordRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public BloodRequestService(
            BloodRequestRepository bloodRequestRepository,
            UserRepository userRepository,
            DonorMatchingService matchingService,
            DonorMatchRepository donorMatchRepository,
            DonationRecordRepository donationRecordRepository,
            NotificationService notificationService,
            Clock clock) {

        this.bloodRequestRepository = bloodRequestRepository;
        this.userRepository = userRepository;
        this.matchingService = matchingService;
        this.donorMatchRepository = donorMatchRepository;
        this.donationRecordRepository = donationRecordRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Transactional
    public BloodRequest createRequest(
            BloodRequestRequest request, String email) {

        AppUser requester = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("User account not found."));

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

        // Tell administrators: Normal requests need approval; Critical
        // requests may optionally be reviewed (D-15, owner decision 2.6).
        notificationService.newRequestForAdmins(savedRequest);

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

    /**
     * The requester's own requests for one status tab on "My Requests"
     * (Module 3.4), with a count for every tab. Filtering happens on the
     * server; each user's list is small, so it is loaded once.
     */
    @Transactional(readOnly = true)
    public MyRequestsView getMyRequests(String email, RequestListTab tab) {

        List<BloodRequest> all = getMyRequests(email);

        Map<RequestListTab, Long> counts = new EnumMap<>(RequestListTab.class);
        for (RequestListTab t : RequestListTab.values()) {
            counts.put(t, all.stream().filter(r -> t.includes(r.getAdminStatus())).count());
        }

        return new MyRequestsView(
                tab,
                all.stream().filter(r -> tab.includes(r.getAdminStatus())).toList(),
                counts);
    }

    /**
     * Closes (cancels) the requester's own Pending or Approved request
     * (FR-09, D-13). The reason is optional for requesters.
     */
    @Transactional
    public void closeRequest(Long requestId, String email, String reason) {

        BloodRequest request = findOwnRequest(requestId, email);

        // FR-19: only Pending or Approved requests can be closed.
        RequestStatusPolicy.requireTransition(
                request.getAdminStatus(), RequestAdminStatus.CLOSED);

        String cleaned = null;
        if (reason != null && !reason.isBlank()) {
            cleaned = reason.trim();
            if (cleaned.length() > MAX_REASON_LENGTH) {
                throw new IllegalArgumentException(
                        "The reason cannot exceed " + MAX_REASON_LENGTH + " characters.");
            }
        }

        // The matching status is kept so the request history shows
        // whether matches had been found before it was closed.
        request.setAdminStatus(RequestAdminStatus.CLOSED);
        request.setCloseReason(cleaned);
        request.setClosedBy(request.getRequester());
        request.setClosedAt(LocalDateTime.now(clock));

        bloodRequestRepository.save(request);

        // Donor contact details stop being visible automatically, because
        // they are only shown for active requests (D-16).
        notificationService.requestEndedForDonors(request, "has been closed.", null);
    }

    /**
     * Requester's detail view of their own request: summary, progress and
     * timeline. No donor identity, health data or administrator identity.
     */
    @Transactional(readOnly = true)
    public RequestDetailView getRequestDetail(Long requestId, String email) {

        BloodRequest request = findOwnRequest(requestId, email);

        List<DonorMatch> matches =
                donorMatchRepository.findAllWithDonorByRequestId(requestId);

        boolean active = RequestStatusPolicy.isActive(request.getAdminStatus());

        // Deactivated donors no longer count as accepted, and their
        // contact details are hidden (owner decision, Module 2.5).
        long accepted = matches.stream()
                .filter(m -> m.getDonorResponse() == DonorResponse.ACCEPTED
                        && m.getDonor().isActive())
                .count();

        long shared = active
                ? matches.stream()
                        .filter(m -> m.getDonorResponse() == DonorResponse.ACCEPTED
                                && m.isDonorConsent()
                                && m.getDonor().isActive())
                        .count()
                : 0;

        int fulfilled = request.getFulfilledUnits() == null ? 0 : request.getFulfilledUnits();

        return new RequestDetailView(
                request.getId(),
                request.getBloodGroup().getLabel(),
                request.getUnitsRequired(),
                fulfilled,
                request.getCity(),
                request.getHospitalName(),
                request.getRequiredAt(),
                request.getUrgency().getLabel(),
                request.getUrgency() == RequestUrgency.CRITICAL,
                request.getAdminStatus().getLabel(),
                request.getAdminStatus().name(),
                request.getMatchingStatus().getLabel(),
                request.getRejectionReason(),
                request.getCloseReason(),
                matches.size(),
                accepted,
                shared,
                RequestStatusPolicy.canTransition(
                        request.getAdminStatus(), RequestAdminStatus.CLOSED),
                buildTimeline(request, matches.size(), accepted, fulfilled),
                RequestStage.of(request.getAdminStatus(), request.getUrgency(),
                        request.getMatchingStatus()));
    }

    // ---------------- HELPERS ----------------

    private BloodRequest findOwnRequest(Long requestId, String email) {
        return bloodRequestRepository
                .findByIdAndRequesterEmail(requestId, email)
                .orElseThrow(() ->
                        new NotFoundException("Blood request not found."));
    }

    private List<TimelineEntry> buildTimeline(
            BloodRequest request, long matchCount, long accepted, int fulfilled) {

        List<TimelineEntry> timeline = new ArrayList<>();
        RequestAdminStatus status = request.getAdminStatus();
        boolean critical = request.getUrgency() == RequestUrgency.CRITICAL;

        // 1. Submitted
        timeline.add(new TimelineEntry(
                "Request submitted",
                request.getUnitsRequired() + " unit(s) of "
                        + request.getBloodGroup().getLabel() + " needed at "
                        + request.getHospitalName() + ".",
                request.getCreatedAt(),
                State.DONE));

        // 2. Administrator review (optional for Critical requests, D-15)
        if (request.getReviewedAt() != null) {
            boolean rejected = status == RequestAdminStatus.REJECTED;
            timeline.add(new TimelineEntry(
                    rejected ? "Rejected by an administrator" : "Approved by an administrator",
                    rejected ? "Reason: " + request.getRejectionReason() : null,
                    request.getReviewedAt(),
                    State.DONE));
        } else if (status == RequestAdminStatus.PENDING) {
            timeline.add(new TimelineEntry(
                    critical ? "Administrator review (optional)" : "Awaiting administrator approval",
                    critical
                            ? "Critical requests are matched immediately; an administrator may still review it."
                            : "Matching starts after an administrator approves this request.",
                    null,
                    critical ? State.PENDING : State.CURRENT));
        }

        // 3. Matching
        boolean matchingStarted = request.getMatchingStatus() != MatchingStatus.NOT_STARTED;
        timeline.add(new TimelineEntry(
                "Donor matching",
                matchingStarted
                        ? matchCount + " potential donor(s) found, " + accepted + " accepted."
                        : "Not started yet.",
                null,
                matchingStarted
                        ? (RequestStatusPolicy.isActive(status) ? State.CURRENT : State.DONE)
                        : State.PENDING));

        // 4. Donations
        LocalDateTime lastDonation = donationRecordRepository
                .findAllByRequestId(request.getId())
                .stream()
                .filter(dr -> dr.getOutcome() == DonationOutcome.COMPLETED)
                .map(DonationRecord::getReportedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        timeline.add(new TimelineEntry(
                status == RequestAdminStatus.FULFILLED ? "Request fulfilled" : "Donations",
                fulfilled + " of " + request.getUnitsRequired() + " unit(s) donated.",
                lastDonation,
                status == RequestAdminStatus.FULFILLED
                        ? State.DONE
                        : (fulfilled > 0 ? State.CURRENT : State.PENDING)));

        // 5. Closed
        if (status == RequestAdminStatus.CLOSED) {
            boolean closedByRequester = request.getClosedBy() != null
                    && request.getClosedBy().getId().equals(request.getRequester().getId());

            String who = request.getClosedBy() == null
                    ? "Closed"
                    : (closedByRequester ? "Closed by you" : "Closed by an administrator");

            timeline.add(new TimelineEntry(
                    who,
                    request.getCloseReason() == null ? null : "Reason: " + request.getCloseReason(),
                    request.getClosedAt(),
                    State.DONE));
        }

        return timeline;
    }
}
