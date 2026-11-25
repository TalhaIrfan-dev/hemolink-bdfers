
package com.hemolink.bdfers.service;

import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.Notification;
import com.hemolink.bdfers.enums.AdminApprovalStatus;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonorRequestService {

    private final DonorMatchRepository matchRepository;
    private final NotificationRepository notificationRepository;

    public DonorRequestService(
            DonorMatchRepository matchRepository,
            NotificationRepository notificationRepository) {
        this.matchRepository = matchRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<DonorMatch> getMyMatchedRequests(String email) {
        return matchRepository.findAllByDonorUserEmailOrderByIdDesc(email);
    }

    @Transactional
    public void respondToRequest(
            Long matchId, DonorResponse response, String email) {

        if (response != DonorResponse.ACCEPTED
                && response != DonorResponse.DECLINED) {
            throw new IllegalArgumentException("Invalid donor response.");
        }

        DonorMatch match = getDonorMatch(matchId, email);

        if (match.getDonorResponse() != DonorResponse.PENDING) {
            throw new IllegalStateException(
                    "You have already responded to this request.");
        }

        validateActiveRequest(match);

        match.setDonorResponse(response);

        // Reset disclosure consent and approval on a response change.
        match.setDonorConsent(false);
        match.setConsentAt(null);
        match.setAdminApproval(AdminApprovalStatus.PENDING);
        match.setApprovedBy(null);
        match.setApprovalAt(null);

        matchRepository.save(match);

        Notification notification = new Notification();
        notification.setUser(match.getBloodRequest().getRequester());
        notification.setBloodRequest(match.getBloodRequest());

        if (response == DonorResponse.ACCEPTED) {
            notification.setMessage(
                    "A matched donor has accepted your blood request. "
                    + "Their contact information remains protected.");
        } else {
            notification.setMessage(
                    "A matched donor has declined your blood request.");
        }

        notificationRepository.save(notification);
    }

    @Transactional
    public void giveContactConsent(Long matchId, String email) {

        DonorMatch match = getDonorMatch(matchId, email);

        if (match.getDonorResponse() != DonorResponse.ACCEPTED) {
            throw new IllegalStateException(
                    "You must accept the request before giving contact consent.");
        }

        validateActiveRequest(match);

        if (match.isDonorConsent()) {
            throw new IllegalStateException(
                    "Contact-sharing consent has already been recorded.");
        }

        match.setDonorConsent(true);
        match.setConsentAt(LocalDateTime.now());

        // Admin approval is still required separately.
        match.setAdminApproval(AdminApprovalStatus.PENDING);
        match.setApprovedBy(null);
        match.setApprovalAt(null);

        matchRepository.save(match);

        Notification notification = new Notification();
        notification.setUser(match.getBloodRequest().getRequester());
        notification.setBloodRequest(match.getBloodRequest());
        notification.setMessage(
                "A donor has consented to contact sharing. "
                + "Administrator approval is still required.");

        notificationRepository.save(notification);
    }

    private DonorMatch getDonorMatch(Long matchId, String email) {
        return matchRepository.findByIdAndDonorUserEmail(matchId, email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Matched request not found."));
    }

    private void validateActiveRequest(DonorMatch match) {
        RequestAdminStatus status =
                match.getBloodRequest().getAdminStatus();

        if (status == RequestAdminStatus.CLOSED
                || status == RequestAdminStatus.REJECTED
                || status == RequestAdminStatus.FULFILLED) {
            throw new IllegalStateException(
                    "This blood request is no longer active.");
        }
    }
}
