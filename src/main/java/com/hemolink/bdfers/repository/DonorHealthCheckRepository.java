package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonorHealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonorHealthCheckRepository
        extends JpaRepository<DonorHealthCheck, Long> {

    Optional<DonorHealthCheck> findByMatch_Id(Long matchId);

    /** Health checks for every match of a request (admin only, D-04). */
    @Query("""
        SELECT hc
        FROM DonorHealthCheck hc
        WHERE hc.match.bloodRequest.id = :requestId
        """)
    List<DonorHealthCheck> findAllByRequestId(
            @Param("requestId") Long requestId);

    /** Health checks for one donor (admin donor detail, D-04). */
    @Query("""
        SELECT hc
        FROM DonorHealthCheck hc
        WHERE hc.match.donor.id = :donorId
        """)
    List<DonorHealthCheck> findAllByDonorId(
            @Param("donorId") Long donorId);

    /** Admin dashboard (D-12): acceptances against advice since a date. */
    long countByAcceptedAgainstAdviceTrueAndCreatedAtAfter(LocalDateTime since);
}
