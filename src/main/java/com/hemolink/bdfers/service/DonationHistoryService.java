package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.response.DonationHistoryView;
import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.entity.DonationRecord;
import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.DonationOutcome;
import com.hemolink.bdfers.enums.GuidanceKey;
import com.hemolink.bdfers.repository.DonationRecordRepository;
import com.hemolink.bdfers.repository.DonorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Donor donation history and the advisory next suggested donation date. */
@Service
public class DonationHistoryService {

    private final DonationRecordRepository donationRecordRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final GuidanceSettingsService settingsService;

    public DonationHistoryService(
            DonationRecordRepository donationRecordRepository,
            DonorProfileRepository donorProfileRepository,
            GuidanceSettingsService settingsService) {

        this.donationRecordRepository = donationRecordRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public DonationHistoryView getHistory(String donorEmail) {

        List<DonationHistoryView.Row> rows = donationRecordRepository
                .findHistoryByDonorEmail(donorEmail)
                .stream()
                .map(DonationHistoryService::toRow)
                .toList();

        long completed = rows.stream().filter(DonationHistoryView.Row::isCompleted).count();
        long units = rows.stream().mapToLong(DonationHistoryView.Row::getUnitsDonated).sum();

        LocalDate lastDonation = donorProfileRepository.findByUserEmail(donorEmail)
                .map(DonorProfile::getLastDonationDate)
                .orElse(null);

        return new DonationHistoryView(
                rows,
                completed,
                units,
                lastDonation,
                nextSuggestedDate(lastDonation, settingsService.current()),
                intervalDays(settingsService.current()));
    }

    /**
     * Last donation date plus the configured interval, or null when no
     * interval is configured or no donation is recorded. Advisory only.
     */
    static LocalDate nextSuggestedDate(LocalDate lastDonation, GuidanceSettings settings) {
        Integer interval = intervalDays(settings);
        return lastDonation == null || interval == null
                ? null
                : lastDonation.plusDays(interval);
    }

    private static Integer intervalDays(GuidanceSettings settings) {
        BigDecimal value = settings.get(GuidanceKey.MIN_DAYS_BETWEEN_DONATIONS);
        return value == null ? null : value.intValue();
    }

    private static DonationHistoryView.Row toRow(DonationRecord record) {
        BloodRequest request = record.getMatch().getBloodRequest();
        boolean completed = record.getOutcome() == DonationOutcome.COMPLETED;

        return new DonationHistoryView.Row(
                request.getId(),
                request.getBloodGroup().getLabel(),
                request.getHospitalName(),
                request.getCity(),
                record.getOutcome().getLabel(),
                completed,
                record.getUnitsDonated() == null ? 0 : record.getUnitsDonated(),
                record.getDonationDate(),
                record.getReportedAt());
    }
}
