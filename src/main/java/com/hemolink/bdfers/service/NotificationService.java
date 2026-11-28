package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.response.NotificationView;
import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.entity.Notification;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.NotificationType;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.enums.UserRole;
import com.hemolink.bdfers.exception.NotFoundException;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.NotificationRepository;
import com.hemolink.bdfers.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates, lists and marks in-app notifications (FR-13). In-app only:
 * no email, SMS or push (SRS scope).
 *
 * Privacy rules for messages:
 * <ul>
 *   <li>never include health data;</li>
 *   <li>never name a donor in a message to a requester;</li>
 *   <li>never name an administrator in a message to a requester or donor.</li>
 * </ul>
 */
@Service
public class NotificationService {

    public static final int PAGE_SIZE = 20;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DonorMatchRepository donorMatchRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            DonorMatchRepository donorMatchRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.donorMatchRepository = donorMatchRepository;
    }

    // =====================================================================
    // Creating notifications
    // =====================================================================

    /** Creates one notification. */
    @Transactional
    public void notify(AppUser user, BloodRequest request,
                       NotificationType type, String message) {
        notificationRepository.save(build(user, request, type, message));
    }

    /** Builds (without saving) a notification, for batch saving. */
    public Notification build(AppUser user, BloodRequest request,
                              NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setBloodRequest(request);
        notification.setType(type);
        notification.setMessage(truncate(message));
        return notification;
    }

    @Transactional
    public void saveAll(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }

    /** New request: tell every active administrator (owner decision, 2.6). */
    @Transactional
    public void newRequestForAdmins(BloodRequest request) {

        boolean critical = request.getUrgency() == RequestUrgency.CRITICAL;

        String message = critical
                ? "New critical blood request " + id(request) + " (" + summary(request)
                  + "). Matching has started; review is optional."
                : "New blood request " + id(request) + " (" + summary(request)
                  + ") is awaiting your approval.";

        List<Notification> notes = new ArrayList<>();
        for (AppUser admin : userRepository.findAllByRoleAndActiveTrue(UserRole.ADMIN)) {
            notes.add(build(admin, request, NotificationType.ADMIN_QUEUE, message));
        }
        notificationRepository.saveAll(notes);
    }

    /** Admin approved or rejected a request: tell the requester. */
    @Transactional
    public void requestReviewed(BloodRequest request, boolean approved, String reason) {

        String message;
        if (approved) {
            message = request.getUrgency() == RequestUrgency.NORMAL
                    ? "Your blood request " + id(request)
                      + " has been approved. Matching donors are being notified."
                    : "Your critical blood request " + id(request)
                      + " has been reviewed and approved.";
        } else {
            message = "Your blood request " + id(request)
                    + " was rejected by an administrator. Reason: " + reason;
        }

        notify(request.getRequester(), request, NotificationType.REVIEW, message);
    }

    /** Admin closed a request: tell the requester. */
    @Transactional
    public void requestClosedByAdmin(BloodRequest request, String reason) {
        notify(request.getRequester(), request, NotificationType.REQUEST_UPDATE,
                "Your blood request " + id(request)
                + " was closed by an administrator. Reason: " + reason);
    }

    /**
     * A request ended (rejected, closed or fulfilled): tell matched donors
     * who were waiting or had accepted, and whose profile is active.
     *
     * @param excludeMatchId match to skip (e.g. the donor who just reported), or null
     */
    @Transactional
    public void requestEndedForDonors(BloodRequest request, String endedText, Long excludeMatchId) {

        String message = "Blood request " + id(request) + " (" + summary(request) + ") "
                + endedText + " You do not need to take any further action for it.";

        List<Notification> notes = new ArrayList<>();
        for (DonorMatch match : donorMatchRepository.findMatchesWithDonorDetails(request.getId())) {
            if (match.getDonorResponse() == DonorResponse.DECLINED
                    || !match.getDonor().isActive()
                    || match.getId().equals(excludeMatchId)) {
                continue;
            }
            notes.add(build(match.getDonor().getUser(), request,
                    NotificationType.REQUEST_UPDATE, message));
        }
        notificationRepository.saveAll(notes);
    }

    /**
     * Admin deactivated a donor: tell the donor (with the reason) and tell
     * requesters of active requests where this donor had shared contact
     * details that those details are no longer available.
     */
    @Transactional
    public void donorDeactivated(DonorProfile donor, String reason) {

        notify(donor.getUser(), null, NotificationType.ACCOUNT,
                "Your donor profile was deactivated by an administrator. Reason: " + reason
                + " You will not be matched to new requests while it is deactivated.");

        List<Notification> notes = new ArrayList<>();
        Set<Long> notifiedRequests = new HashSet<>();

        for (DonorMatch match : donorMatchRepository.findAllWithRequestByDonorId(donor.getId())) {
            BloodRequest request = match.getBloodRequest();

            boolean shared = match.getDonorResponse() == DonorResponse.ACCEPTED
                    && match.isDonorConsent();

            if (shared
                    && RequestStatusPolicy.isActive(request.getAdminStatus())
                    && notifiedRequests.add(request.getId())) {

                notes.add(build(request.getRequester(), request,
                        NotificationType.REQUEST_UPDATE,
                        "A donor who shared contact details for your blood request "
                        + id(request) + " is no longer available. Their details have been hidden."));
            }
        }
        notificationRepository.saveAll(notes);
    }

    /** Admin reactivated a donor: tell the donor. */
    @Transactional
    public void donorReactivated(DonorProfile donor) {
        notify(donor.getUser(), null, NotificationType.ACCOUNT,
                "Your donor profile has been reactivated. You can be matched to requests again.");
    }

    // =====================================================================
    // Reading notifications
    // =====================================================================

    @Transactional(readOnly = true)
    public Page<NotificationView> getPage(String email, boolean unreadOnly, int page) {

        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);

        Page<Notification> notifications = unreadOnly
                ? notificationRepository.findAllByUserEmailAndReadFalseOrderByIdDesc(email, pageable)
                : notificationRepository.findAllByUserEmailOrderByIdDesc(email, pageable);

        return notifications.map(n -> new NotificationView(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt(),
                n.getBloodRequest() == null ? null : n.getBloodRequest().getId()));
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return notificationRepository.countByUserEmailAndReadFalse(email);
    }

    /**
     * Marks one of the user's notifications as read and returns the page
     * it points to, chosen by the reader's role:
     * admin → admin request page; requester → their request page;
     * donor → matched requests; account notices → donor profile.
     */
    @Transactional
    public String open(Long notificationId, String email) {

        Notification notification = notificationRepository
                .findByIdAndUserEmail(notificationId, email)
                .orElseThrow(() -> new NotFoundException("Notification not found."));

        notification.setRead(true);
        notificationRepository.save(notification);

        AppUser reader = notification.getUser();
        BloodRequest request = notification.getBloodRequest();

        if (request == null) {
            return reader.getRole() == UserRole.ADMIN ? "/admin/dashboard" : "/donor/profile";
        }
        if (reader.getRole() == UserRole.ADMIN) {
            return "/admin/blood-requests/" + request.getId();
        }
        if (request.getRequester().getId().equals(reader.getId())) {
            return "/requests/" + request.getId();
        }
        return "/donor/requests";
    }

    @Transactional
    public void markAllRead(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> notificationRepository.markAllRead(user.getId()));
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    static String id(BloodRequest request) {
        return String.format("REQ-%04d", request.getId());
    }

    private static String summary(BloodRequest request) {
        return request.getBloodGroup().getLabel() + ", " + request.getUnitsRequired()
                + " unit(s), " + request.getCity();
    }

    /** Messages are stored in a VARCHAR(500) column. */
    private static String truncate(String message) {
        if (message == null) {
            return "";
        }
        return message.length() <= 500 ? message : message.substring(0, 497) + "...";
    }
}
