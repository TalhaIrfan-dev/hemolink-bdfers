
package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonationRecordRepository
        extends JpaRepository<DonationRecord, Long> {

    Optional<DonationRecord> findByMatch_Id(Long matchId);

    boolean existsByMatch_Id(Long matchId);

    /** Donation reports for every match of a request (admin detail page). */
    @Query("""
        SELECT dr
        FROM DonationRecord dr
        WHERE dr.match.bloodRequest.id = :requestId
        ORDER BY dr.id ASC
        """)
    List<DonationRecord> findAllByRequestId(
            @Param("requestId") Long requestId);

    /** Donation reports for one donor (admin donor detail). */
    @Query("""
        SELECT dr
        FROM DonationRecord dr
        WHERE dr.match.donor.id = :donorId
        """)
    List<DonationRecord> findAllByDonorId(
            @Param("donorId") Long donorId);

    /** Donor's own donation history, newest first, with the request loaded. */
    @Query("""
        SELECT dr
        FROM DonationRecord dr
        JOIN FETCH dr.match m
        JOIN FETCH m.bloodRequest
        WHERE m.donor.user.email = :email
        ORDER BY dr.id DESC
        """)
    List<DonationRecord> findHistoryByDonorEmail(
            @Param("email") String email);

    @Query("""
        SELECT dr.match.id
        FROM DonationRecord dr
        WHERE dr.match.donor.user.email = :email
        """)
    List<Long> findReportedMatchIdsByDonorEmail(
            @Param("email") String email);
}
