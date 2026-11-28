package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonationGuidanceSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationGuidanceSettingRepository
        extends JpaRepository<DonationGuidanceSetting, String> {
}
