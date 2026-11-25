package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonorProfileRepository
        extends JpaRepository<DonorProfile, Long> {

    Optional<DonorProfile> findByUserEmail(String email);

    boolean existsByUserEmail(String email);
}