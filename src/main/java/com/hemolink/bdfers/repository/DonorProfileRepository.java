
package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.BloodGroup;
import com.hemolink.bdfers.enums.AvailabilityStatus;
import com.hemolink.bdfers.enums.EligibilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorProfileRepository
        extends JpaRepository<DonorProfile, Long> {

    Optional<DonorProfile> findByUserEmail(String email);

    boolean existsByUserEmail(String email);

    List<DonorProfile> findAllByBloodGroupAndCityIgnoreCaseAndAvailabilityAndEligibilityStatusAndActiveTrue(
            BloodGroup bloodGroup,
            String city,
            AvailabilityStatus availability,
            EligibilityStatus eligibilityStatus
    );
}
