package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonorMatch;
import com.hemolink.bdfers.enums.DonorResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonorMatchRepository
        extends JpaRepository<DonorMatch, Long> {

    /** All matches for a request with the donor profile loaded, oldest first. */
    @Query("""
        SELECT dm FROM DonorMatch dm
        JOIN FETCH dm.donor
        WHERE dm.bloodRequest.id = :requestId
        ORDER BY dm.id ASC
        """)
    List<DonorMatch> findAllWithDonorByRequestId(
            @Param("requestId") Long requestId);

    long countByBloodRequestId(Long requestId);

    Optional<DonorMatch> findByBloodRequestIdAndDonorId(
            Long requestId, Long donorId);

    boolean existsByBloodRequestIdAndDonorId(
            Long requestId, Long donorId);

    List<DonorMatch> findAllByDonorUserEmailOrderByIdDesc(
            String email);

    /** Every match for one donor with the request loaded (admin donor detail). */
    @Query("""
        SELECT dm FROM DonorMatch dm
        JOIN FETCH dm.bloodRequest
        WHERE dm.donor.id = :donorId
        ORDER BY dm.id DESC
        """)
    List<DonorMatch> findAllWithRequestByDonorId(
            @Param("donorId") Long donorId);

    /** Creation times of matches in a range; email null means all requests. */
    @Query("""
        SELECT dm.createdAt
        FROM DonorMatch dm
        WHERE dm.createdAt >= :from
          AND dm.createdAt < :to
          AND (:email IS NULL OR dm.bloodRequest.requester.email = :email)
        """)
    List<LocalDateTime> findCreatedAtBetween(
            @Param("email") String email,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** All matches on a requester's own requests, with donor and request loaded. */
    @Query("""
        SELECT dm FROM DonorMatch dm
        JOIN FETCH dm.donor
        JOIN FETCH dm.bloodRequest br
        WHERE br.requester.email = :email
        """)
    List<DonorMatch> findAllForRequester(@Param("email") String email);

    /** All matches for a donor, with the request loaded. */
    @Query("""
        SELECT dm FROM DonorMatch dm
        JOIN FETCH dm.bloodRequest
        WHERE dm.donor.user.email = :email
        ORDER BY dm.id DESC
        """)
    List<DonorMatch> findAllForDonor(@Param("email") String email);

    Optional<DonorMatch> findByIdAndDonorUserEmail(
            Long matchId, String email);

    /** IDs of donors already matched to a request (one query, NFR-01). */
    @Query("""
        SELECT dm.donor.id
        FROM DonorMatch dm
        WHERE dm.bloodRequest.id = :requestId
        """)
    List<Long> findDonorIdsByRequestId(
            @Param("requestId") Long requestId);

    /** Matches for a request with donor and user loaded (contact view). */
    @Query("""
        SELECT dm FROM DonorMatch dm
        JOIN FETCH dm.donor d
        JOIN FETCH d.user
        WHERE dm.bloodRequest.id = :requestId
        ORDER BY dm.consentAt ASC
        """)
    List<DonorMatch> findMatchesWithDonorDetails(
            @Param("requestId") Long requestId);

    /**
     * Administrator disclosure log (D-16): every match where the donor
     * accepted and consented to share contact details, newest first.
     */
    @Query("""
        SELECT dm FROM DonorMatch dm
        JOIN FETCH dm.bloodRequest br
        JOIN FETCH br.requester
        JOIN FETCH dm.donor d
        JOIN FETCH d.user
        WHERE dm.donorResponse = :accepted
          AND dm.donorConsent = true
        ORDER BY dm.consentAt DESC
        """)
    List<DonorMatch> findDisclosureLog(
            @Param("accepted") DonorResponse accepted);
}
