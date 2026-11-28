
package com.hemolink.bdfers.service;

import com.hemolink.bdfers.enums.NotificationType;

import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.exception.NotFoundException;

import com.hemolink.bdfers.dto.response.DonorRequestRow;
import com.hemolink.bdfers.dto.response.DonorRequestsView;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.Notification;
import com.hemolink.bdfers.enums.DonorRequestTab;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class DonorRequestService {

    private final DonorMatchRepository matchRepository;
    private final NotificationService notificationService;

    public DonorRequestService(
            DonorMatchRepository matchRepository,
            NotificationService notificationService) {
        this.matchRepository = matchRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<DonorMatch> getMyMatchedRequests(String email) {
        return matchRepository.findAllByDonorUserEmailOrderByIdDesc(email);
    }

    /**
     * Matched requests for the "Matched Requests" page (Module 3.5), as rows
     * with the actions the donor may take, filtered to one tab on the server.
     * When no tab is chosen, the first tab with items is shown.
     *
     * @param reportedMatchIds matches the donor has already reported an outcome for
     */
    @Transactional(readOnly = true)
    public DonorRequestsView getMyRequestsView(String email, DonorRequestTab chosen,
                                               Set<Long> reportedMatchIds) {

        List<DonorRequestRow> all = getMyMatchedRequests(email).stream()
                .map(m -> toRow(m, reportedMatchIds.contains(m.getId())))
                .toList();

        long waiting = all.stream().filter(r -> r.getTab() == DonorRequestTab.WAITING).count();
        long accepted = all.stream().filter(r -> r.getTab() == DonorRequestTab.ACCEPTED).count();
        long past = all.size() - waiting - accepted;

        DonorRequestTab tab = chosen;
        if (tab == null) {
            tab = waiting > 0 ? DonorRequestTab.WAITING
                    : accepted > 0 ? DonorRequestTab.ACCEPTED
                    : past > 0 ? DonorRequestTab.PAST
                    : DonorRequestTab.WAITING;
        }
        DonorRequestTab selected = tab;

        return new DonorRequestsView(
                selected,
                all.stream().filter(r -> r.getTab() == selected).toList(),
                waiting, accepted, past);
    }

    private static DonorRequestRow toRow(DonorMatch match, boolean reported) {

        BloodRequest request = match.getBloodRequest();
        boolean requestActive = RequestStatusPolicy.isActive(request.getAdminStatus());
        boolean actionsAllowed = RequestStatusPolicy.acceptsDonorActions(
                request.getAdminStatus(), request.getUrgency())
                && match.getDonor().isActive();

        DonorResponse response = match.getDonorResponse();
        boolean isAccepted = response == DonorResponse.ACCEPTED;

        return new DonorRequestRow(
                match.getId(),
                request.getId(),
                request.getBloodGroup().getLabel(),
                request.getUnitsRequired() == null ? 0 : request.getUnitsRequired(),
                request.getFulfilledUnits() == null ? 0 : request.getFulfilledUnits(),
                request.getCity(),
                request.getHospitalName(),
                request.getRequiredAt(),
                request.getUrgency() == RequestUrgency.CRITICAL,
                request.getUrgency().getLabel(),
                request.getAdminStatus().getLabel(),
                response.name(),
                response.getLabel(),
                match.isDonorConsent(),
                match.getConsentAt(),
                match.getCreatedAt(),
                requestActive,
                actionsAllowed && response == DonorResponse.PENDING,
                actionsAllowed && isAccepted && !match.isDonorConsent(),
                actionsAllowed && isAccepted && !reported,
                reported,
                DonorRequestTab.of(response, requestActive));
    }

    @Transactional
    public void respondToRequest(
            Long matchId, DonorResponse response, String email) {

        if (response != DonorResponse.ACCEPTED
                && response != DonorResponse.DECLINED) {
            throw new IllegalArgumentException("Invalid donor response.");
        }

        // Accepting goes through the advisory health check (D-01).
        if (response == DonorResponse.ACCEPTED) {
            throw new BusinessRuleException(
                    "Please complete the short health check to accept this request.");
        }

        DonorMatch match = getDonorMatch(matchId, email);

        if (match.getDonorResponse() != DonorResponse.PENDING) {
            throw new BusinessRuleException(
                    "You have already responded to this request.");
        }

        validateDonorActionsAllowed(match);

        match.setDonorResponse(response);

        // Consent is given separately after accepting (FR-15).
        match.setDonorConsent(false);
        match.setConsentAt(null);

        matchRepository.save(match);

        Notification notification = new Notification();
        notification.setUser(match.getBloodRequest().getRequester());
        notification.setBloodRequest(match.getBloodRequest());
        notification.setType(NotificationType.RESPONSE);

        if (response == DonorResponse.ACCEPTED) {
            notification.setMessage(
                    "A matched donor has accepted your blood request "
                    + formatRequestId(match) + ". Their contact details "
                    + "stay hidden until they choose to share them.");
        } else {
            notification.setMessage(
                    "A matched donor has declined your blood request "
                    + formatRequestId(match) + ".");
        }

        notificationService.saveAll(List.of(notification));
    }

    @Transactional
    public void giveContactConsent(Long matchId, String email) {

        DonorMatch match = getDonorMatch(matchId, email);

        if (match.getDonorResponse() != DonorResponse.ACCEPTED) {
            throw new BusinessRuleException(
                    "You must accept the request before giving contact consent.");
        }

        validateDonorActionsAllowed(match);

        if (match.isDonorConsent()) {
            throw new BusinessRuleException(
                    "Contact-sharing consent has already been recorded.");
        }

        // D-16: acceptance + explicit consent releases the donor's name
        // and phone number to this requester while the request is active.
        // No administrator approval is required.
        match.setDonorConsent(true);
        match.setConsentAt(LocalDateTime.now());

        matchRepository.save(match);

        Notification notification = new Notification();
        notification.setUser(match.getBloodRequest().getRequester());
        notification.setBloodRequest(match.getBloodRequest());
        notification.setType(NotificationType.CONSENT);
        notification.setMessage(
                "A donor has shared their contact details for your blood request "
                + formatRequestId(match) + ". View them under Donor Contacts.");

        notificationService.saveAll(List.of(notification));
    }

    private DonorMatch getDonorMatch(Long matchId, String email) {
        return matchRepository.findByIdAndDonorUserEmail(matchId, email)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Matched request not found."));
    }

    /**
     * D-15: Critical requests accept donor actions while Pending or
     * Approved; Normal requests only when Approved; never once Rejected,
     * Closed or Fulfilled. Deactivated donors cannot act (FR-17).
     */
    private void validateDonorActionsAllowed(DonorMatch match) {

        if (!match.getDonor().isActive()) {
            throw new BusinessRuleException(
                    "Your donor profile has been deactivated by an administrator, "
                    + "so you cannot respond to requests.");
        }

        RequestStatusPolicy.requireDonorActionsAllowed(
                match.getBloodRequest().getAdminStatus(),
                match.getBloodRequest().getUrgency());
    }

    private static String formatRequestId(DonorMatch match) {
        return String.format("REQ-%04d", match.getBloodRequest().getId());
    }
}
