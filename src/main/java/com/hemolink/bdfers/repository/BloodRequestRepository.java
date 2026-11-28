
package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import com.hemolink.bdfers.enums.RequestUrgency;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodRequestRepository
        extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findAllByRequesterEmailOrderByCreatedAtDesc(
            String email);

    Optional<BloodRequest> findByIdAndRequesterEmail(
            Long id,
            String email);

    List<BloodRequest> findAllByAdminStatusOrderByCreatedAtDesc(
            RequestAdminStatus adminStatus);

    List<BloodRequest> findAllByOrderByCreatedAtDesc();

    /**
     * Admin request list with optional filters (FR-16). Each filter is
     * ignored when null. Sorting is supplied through the Pageable.
     */
    @Query(value = """
        SELECT br
        FROM BloodRequest br
        JOIN FETCH br.requester
        WHERE (:status IS NULL OR br.adminStatus = :status)
          AND (:urgency IS NULL OR br.urgency = :urgency)
          AND (:city IS NULL OR LOWER(br.city) = LOWER(:city))
        """,
        countQuery = """
        SELECT COUNT(br)
        FROM BloodRequest br
        WHERE (:status IS NULL OR br.adminStatus = :status)
          AND (:urgency IS NULL OR br.urgency = :urgency)
          AND (:city IS NULL OR LOWER(br.city) = LOWER(:city))
        """)
    Page<BloodRequest> search(
            @Param("status") RequestAdminStatus status,
            @Param("urgency") RequestUrgency urgency,
            @Param("city") String city,
            Pageable pageable);

    /** Cities that have at least one request, for the admin city filter. */
    @Query("""
        SELECT DISTINCT br.city
        FROM BloodRequest br
        ORDER BY br.city
        """)
    List<String> findDistinctCities();

    // ---------------- Dashboards (Module 2.7) ----------------

    /** Creation times of requests in a range; email null means all requests. */
    @Query("""
        SELECT br.createdAt
        FROM BloodRequest br
        WHERE br.createdAt >= :from
          AND br.createdAt < :to
          AND (:email IS NULL OR br.requester.email = :email)
        """)
    List<LocalDateTime> findCreatedAtBetween(
            @Param("email") String email,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** Requests per blood group; email null means all requests. Rows: [BloodGroup, Long]. */
    @Query("""
        SELECT br.bloodGroup, COUNT(br)
        FROM BloodRequest br
        WHERE (:email IS NULL OR br.requester.email = :email)
        GROUP BY br.bloodGroup
        """)
    List<Object[]> countByBloodGroup(@Param("email") String email);

    List<BloodRequest> findTop5ByRequesterEmailOrderByIdDesc(String email);

    /** Unreviewed Critical requests, oldest first (admin queue). */
    @EntityGraph(attributePaths = "requester")
    List<BloodRequest> findTop5ByAdminStatusAndUrgencyOrderByCreatedAtAsc(
            RequestAdminStatus adminStatus, RequestUrgency urgency);

    @EntityGraph(attributePaths = "reviewedBy")
    List<BloodRequest> findTop5ByReviewedAtIsNotNullOrderByReviewedAtDesc();

    @EntityGraph(attributePaths = "closedBy")
    List<BloodRequest> findTop5ByClosedAtIsNotNullOrderByClosedAtDesc();

    long countByAdminStatusAndCreatedAtGreaterThanEqual(
            RequestAdminStatus adminStatus, LocalDateTime since);

    // Admin dashboard counts (D-12)
    long countByAdminStatus(RequestAdminStatus adminStatus);

    long countByAdminStatusAndUrgency(
            RequestAdminStatus adminStatus, RequestUrgency urgency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT br
        FROM BloodRequest br
        WHERE br.id = :requestId
        """)
    Optional<BloodRequest> findByIdForUpdate(
            @Param("requestId") Long requestId);
}
