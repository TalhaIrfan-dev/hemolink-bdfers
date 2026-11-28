package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.response.ActivityItem;
import com.hemolink.bdfers.dto.response.AdminDashboard;
import com.hemolink.bdfers.dto.response.DonorDashboard;
import com.hemolink.bdfers.dto.response.LabelCount;
import com.hemolink.bdfers.dto.response.NotificationView;
import com.hemolink.bdfers.dto.response.RequesterDashboard;
import com.hemolink.bdfers.dto.response.TrendData;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.entity.DonorStatusChange;
import com.hemolink.bdfers.entity.GuidanceSettingAudit;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.enums.DonationOutcome;
import com.hemolink.bdfers.enums.DonorResponse;
import com.hemolink.bdfers.enums.GuidanceKey;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.enums.RequestUrgency;
import com.hemolink.bdfers.repository.BloodRequestRepository;
import com.hemolink.bdfers.repository.DonationRecordRepository;
import com.hemolink.bdfers.repository.DonorMatchRepository;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import com.hemolink.bdfers.repository.DonorStatusChangeRepository;
import com.hemolink.bdfers.repository.GuidanceSettingAuditRepository;
import com.hemolink.bdfers.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Data for the requester, donor and admin dashboards (Module 2.7).
 *
 * Rules (approved):
 * <ul>
 *   <li>Only real database values; nothing is hard-coded.</li>
 *   <li>"This week" = created in the last 7 days (shown as "+N this week"),
 *       not a percentage.</li>
 *   <li>Requester data covers the user's own requests only and contains no
 *       donor identity.</li>
 * </ul>
 */
@Service
public class DashboardService {

    private final BloodRequestRepository requestRepository;
    private final DonorMatchRepository matchRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final DonationRecordRepository donationRecordRepository;
    private final DonorStatusChangeRepository statusChangeRepository;
    private final GuidanceSettingAuditRepository guidanceAuditRepository;
    private final NotificationRepository notificationRepository;
    private final AdminService adminService;
    private final EligibilityAdvisor advisor;
    private final GuidanceSettingsService settingsService;
    private final Clock clock;

    public DashboardService(
            BloodRequestRepository requestRepository,
            DonorMatchRepository matchRepository,
            DonorProfileRepository donorProfileRepository,
            DonationRecordRepository donationRecordRepository,
            DonorStatusChangeRepository statusChangeRepository,
            GuidanceSettingAuditRepository guidanceAuditRepository,
            NotificationRepository notificationRepository,
            AdminService adminService,
            EligibilityAdvisor advisor,
            GuidanceSettingsService settingsService,
            Clock clock) {

        this.requestRepository = requestRepository;
        this.matchRepository = matchRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.donationRecordRepository = donationRecordRepository;
        this.statusChangeRepository = statusChangeRepository;
        this.guidanceAuditRepository = guidanceAuditRepository;
        this.notificationRepository = notificationRepository;
        this.adminService = adminService;
        this.advisor = advisor;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    // =====================================================================
    // Requester section
    // =====================================================================

    @Transactional(readOnly = true)
    public RequesterDashboard getRequesterDashboard(String email) {

        LocalDateTime weekAgo = weekAgo();

        List<BloodRequest> myRequests =
                requestRepository.findAllByRequesterEmailOrderByCreatedAtDesc(email);

        long active = 0;
        long activeThisWeek = 0;
        long completed = 0;
        long completedThisWeek = 0;

        for (BloodRequest request : myRequests) {
            if (RequestStatusPolicy.isActive(request.getAdminStatus())) {
                active++;
                if (isAfter(request.getCreatedAt(), weekAgo)) {
                    activeThisWeek++;
                }
            }
            if (request.getAdminStatus() == RequestAdminStatus.FULFILLED) {
                completed++;
                // A fulfilled request is not changed again, so its last
                // update time is when it became fulfilled.
                if (isAfter(request.getUpdatedAt(), weekAgo)) {
                    completedThisWeek++;
                }
            }
        }

        // Potential matches: on my active requests, not declined, donor active.
        long potential = 0;
        long potentialThisWeek = 0;
        long awaiting = 0;
        long awaitingThisWeek = 0;

        for (DonorMatch match : matchRepository.findAllForRequester(email)) {
            if (!RequestStatusPolicy.isActive(match.getBloodRequest().getAdminStatus())
                    || match.getDonorResponse() == DonorResponse.DECLINED
                    || !match.getDonor().isActive()) {
                continue;
            }
            boolean recent = isAfter(match.getCreatedAt(), weekAgo);

            potential++;
            if (recent) {
                potentialThisWeek++;
            }
            if (match.getDonorResponse() == DonorResponse.PENDING) {
                awaiting++;
                if (recent) {
                    awaitingThisWeek++;
                }
            }
        }

        List<BloodRequest> urgent = myRequests.stream()
                .filter(r -> r.getUrgency() == RequestUrgency.CRITICAL
                        && RequestStatusPolicy.isActive(r.getAdminStatus()))
                .limit(3)
                .toList();

        List<NotificationView> activity = notificationRepository
                .findTop5ByUserEmailOrderByIdDesc(email)
                .stream()
                .map(n -> new NotificationView(
                        n.getId(), n.getType(), n.getMessage(), n.isRead(), n.getCreatedAt(),
                        n.getBloodRequest() == null ? null : n.getBloodRequest().getId()))
                .toList();

        return new RequesterDashboard(
                active, activeThisWeek,
                potential, potentialThisWeek,
                awaiting, awaitingThisWeek,
                completed, completedThisWeek,
                bloodGroupCounts(email),
                urgent,
                activity,
                requestRepository.findTop5ByRequesterEmailOrderByIdDesc(email));
    }

    // =====================================================================
    // Donor section
    // =====================================================================

    @Transactional(readOnly = true)
    public DonorDashboard getDonorDashboard(String email) {

        DonorProfile profile = donorProfileRepository.findByUserEmail(email).orElse(null);
        if (profile == null) {
            return DonorDashboard.noProfile();
        }

        GuidanceSettings settings = settingsService.current();

        var advice = advisor.assess(
                new EligibilityAdvisor.Input(
                        profile.getDateOfBirth(),
                        profile.getLastDonationDate(),
                        profile.getWeight(),
                        null, null, null),
                settings);

        List<DonorDashboard.PendingItem> pending = new ArrayList<>();
        long awaitingReply = 0;
        long acceptedActive = 0;

        for (DonorMatch match : matchRepository.findAllForDonor(email)) {
            BloodRequest request = match.getBloodRequest();
            boolean actionable = RequestStatusPolicy.acceptsDonorActions(
                    request.getAdminStatus(), request.getUrgency());

            if (match.getDonorResponse() == DonorResponse.PENDING && actionable) {
                awaitingReply++;
                if (pending.size() < 5) {
                    pending.add(new DonorDashboard.PendingItem(
                            match.getId(),
                            request.getId(),
                            request.getBloodGroup().getLabel(),
                            request.getHospitalName(),
                            request.getCity(),
                            request.getUrgency().getLabel(),
                            request.getUrgency() == RequestUrgency.CRITICAL));
                }
            }
            if (match.getDonorResponse() == DonorResponse.ACCEPTED
                    && RequestStatusPolicy.isActive(request.getAdminStatus())) {
                acceptedActive++;
            }
        }

        long completedDonations = donationRecordRepository.findAllByDonorId(profile.getId())
                .stream()
                .filter(dr -> dr.getOutcome() == DonationOutcome.COMPLETED)
                .count();

        return new DonorDashboard(
                true,
                profile.isActive(),
                profile.getAvailability().getLabel(),
                advice,
                profile.isActive() ? awaitingReply : 0,
                profile.isActive() ? pending : List.of(),
                acceptedActive,
                completedDonations,
                profile.getLastDonationDate(),
                DonationHistoryService.nextSuggestedDate(profile.getLastDonationDate(), settings));
    }

    // =====================================================================
    // Admin dashboard
    // =====================================================================

    @Transactional(readOnly = true)
    public AdminDashboard getAdminDashboard() {
        return new AdminDashboard(
                adminService.getDashboardStats(),
                requestRepository.countByAdminStatusAndCreatedAtGreaterThanEqual(
                        RequestAdminStatus.PENDING, weekAgo()),
                bloodGroupCounts(null),
                requestRepository.findTop5ByAdminStatusAndUrgencyOrderByCreatedAtAsc(
                        RequestAdminStatus.PENDING, RequestUrgency.CRITICAL),
                recentAdminActivity());
    }

    /**
     * Latest administrative actions from the audit records: request
     * reviews and closures, donor deactivations/reactivations and guidance
     * setting changes. Newest first, at most 8.
     */
    private List<ActivityItem> recentAdminActivity() {
        List<ActivityItem> items = new ArrayList<>();

        for (BloodRequest r : requestRepository.findTop5ByReviewedAtIsNotNullOrderByReviewedAtDesc()) {
            boolean rejected = r.getAdminStatus() == RequestAdminStatus.REJECTED
                    || r.getRejectionReason() != null;
            items.add(new ActivityItem(
                    "REVIEW",
                    NotificationService.id(r) + (rejected ? " rejected" : " approved"),
                    "by " + nameOf(r.getReviewedBy() == null ? null : r.getReviewedBy().getFullName()),
                    r.getReviewedAt(),
                    "/admin/blood-requests/" + r.getId()));
        }

        for (BloodRequest r : requestRepository.findTop5ByClosedAtIsNotNullOrderByClosedAtDesc()) {
            items.add(new ActivityItem(
                    "CLOSE",
                    NotificationService.id(r) + " closed",
                    "by " + nameOf(r.getClosedBy() == null ? null : r.getClosedBy().getFullName()),
                    r.getClosedAt(),
                    "/admin/blood-requests/" + r.getId()));
        }

        for (DonorStatusChange c : statusChangeRepository.findTop5ByOrderByIdDesc()) {
            items.add(new ActivityItem(
                    "DONOR",
                    "Donor " + c.getDonor().getUser().getFullName() + " "
                            + c.getAction().getLabel().toLowerCase(),
                    "by " + c.getChangedBy().getFullName() + ": " + c.getReason(),
                    c.getChangedAt(),
                    "/admin/donors/" + c.getDonor().getId()));
        }

        for (GuidanceSettingAudit a : guidanceAuditRepository.findTop20ByOrderByIdDesc()
                .stream().limit(5).toList()) {
            String label;
            try {
                label = GuidanceKey.valueOf(a.getSettingKey()).getLabel();
            } catch (IllegalArgumentException ex) {
                label = a.getSettingKey();
            }
            items.add(new ActivityItem(
                    "SETTINGS",
                    "Guidance setting changed: " + label,
                    valueText(a.getOldValue()) + " → " + valueText(a.getNewValue())
                            + " by " + a.getChangedBy().getFullName(),
                    a.getChangedAt(),
                    "/admin/guidance-settings"));
        }

        return items.stream()
                .filter(i -> i.getTime() != null)
                .sorted(Comparator.comparing(ActivityItem::getTime).reversed())
                .limit(8)
                .toList();
    }

    // =====================================================================
    // Trend chart
    // =====================================================================

    /**
     * Requests submitted and donor matches created per bucket.
     *
     * @param requesterEmail the requester's email, or null for all requests (admin)
     */
    @Transactional(readOnly = true)
    public TrendData getTrend(String requesterEmail, String periodParam, Integer yearParam) {

        LocalDate today = LocalDate.now(clock);
        TrendCalculator.Period period = TrendCalculator.parsePeriod(periodParam);
        int year = (yearParam == null || yearParam < 2000 || yearParam > today.getYear() + 1)
                ? today.getYear()
                : yearParam;

        List<TrendCalculator.Bucket> buckets = TrendCalculator.buckets(period, year, today);
        LocalDateTime from = buckets.get(0).start();
        LocalDateTime to = buckets.get(buckets.size() - 1).end();

        return TrendCalculator.build(
                period,
                year,
                buckets,
                requestRepository.findCreatedAtBetween(requesterEmail, from, to),
                matchRepository.findCreatedAtBetween(requesterEmail, from, to));
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    /** Requests per blood group in the standard order; email null = all. */
    private List<LabelCount> bloodGroupCounts(String email) {
        Map<BloodGroup, Long> counts = new EnumMap<>(BloodGroup.class);
        for (Object[] row : requestRepository.countByBloodGroup(email)) {
            counts.put((BloodGroup) row[0], ((Number) row[1]).longValue());
        }

        List<LabelCount> result = new ArrayList<>();
        for (BloodGroup group : BloodGroup.values()) {
            long count = counts.getOrDefault(group, 0L);
            if (count > 0) {
                result.add(new LabelCount(group.getLabel(), count));
            }
        }
        return result;
    }

    private LocalDateTime weekAgo() {
        return LocalDateTime.now(clock).minusDays(7);
    }

    private static boolean isAfter(LocalDateTime time, LocalDateTime since) {
        return time != null && !time.isBefore(since);
    }

    private static String nameOf(String name) {
        return name == null ? "an administrator" : name;
    }

    private static String valueText(java.math.BigDecimal value) {
        return value == null ? "Off" : value.stripTrailingZeros().toPlainString();
    }
}
