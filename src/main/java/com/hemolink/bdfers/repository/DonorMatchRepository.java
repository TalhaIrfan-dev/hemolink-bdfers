package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.enums.AdminApprovalStatus;
import com.hemolink.bdfers.enums.DonorResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonorMatchRepository
        extends JpaRepository<DonorMatch, Long> {

    List<DonorMatch> findAllByBloodRequestId(Long requestId);

    Optional<DonorMatch> findByBloodRequestIdAndDonorId(
            Long requestId, Long donorId);

    boolean existsByBloodRequestIdAndDonorId(
            Long requestId, Long donorId);

    List<DonorMatch> findAllByDonorUserEmailOrderByIdDesc(
            String email);

    Optional<DonorMatch> findByIdAndDonorUserEmail(
            Long matchId, String email);

    List<DonorMatch> findAllByDonorResponseAndDonorConsentTrueAndAdminApprovalOrderByConsentAtDesc(
            DonorResponse response,
            AdminApprovalStatus approval);

    @Query("""
        SELECT dm FROM DonorMatch dm
        JOIN FETCH dm.donor d
        JOIN FETCH d.user
        WHERE dm.bloodRequest.id = :requestId
        """)
    List<DonorMatch> findMatchesWithDonorDetails(
            @Param("requestId") Long requestId);

    @Query("""
        SELECT dm FROM DonorMatch dm
        JOIN FETCH dm.bloodRequest br
        JOIN FETCH br.requester
        JOIN FETCH dm.donor d
        JOIN FETCH d.user
        WHERE dm.id = :matchId
        """)
    Optional<DonorMatch> findMatchWithDetails(
            @Param("matchId") Long matchId);

    @Modifying
    @Query("""
        UPDATE DonorMatch dm
        SET dm.adminApproval = :revokedStatus
        WHERE dm.bloodRequest.id = :requestId
          AND dm.adminApproval = :approvedStatus
        """)
    int revokeApprovedDisclosures(
            @Param("requestId") Long requestId,
            @Param("approvedStatus") AdminApprovalStatus approvedStatus,
            @Param("revokedStatus") AdminApprovalStatus revokedStatus);
}