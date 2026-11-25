package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodRequestRepository
        extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findAllByRequesterEmailOrderByCreatedAtDesc(
            String email
    );

    Optional<BloodRequest> findByIdAndRequesterEmail(
            Long id,
            String email
    );

    List<BloodRequest> findAllByAdminStatusOrderByCreatedAtDesc(
            RequestAdminStatus adminStatus
    );

    List<BloodRequest> findAllByOrderByCreatedAtDesc();
}