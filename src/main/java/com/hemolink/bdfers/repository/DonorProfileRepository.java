package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonorProfile;
import com.hemolink.bdfers.enums.AvailabilityStatus;
import com.hemolink.bdfers.enums.BloodGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonorProfileRepository
        extends JpaRepository<DonorProfile, Long> {

    Optional<DonorProfile> findByUserEmail(String email);

    boolean existsByUserEmail(String email);

    /**
     * Candidate donors for a blood request (FR-10, FR-11):
     * compatible blood group, same city (case-insensitive), the required
     * availability, active donor profile and active user account,
     * excluding the requester's own profile.
     *
     * The user is fetched in the same query so that matching does not
     * issue one extra query per donor (NFR-01).
     */
    @Query("""
        SELECT d
        FROM DonorProfile d
        JOIN FETCH d.user u
        WHERE d.bloodGroup IN :bloodGroups
          AND LOWER(d.city) = LOWER(:city)
          AND d.availability = :availability
          AND d.active = true
          AND u.active = true
          AND u.id <> :requesterId
        """)
    List<DonorProfile> findMatchCandidates(
            @Param("bloodGroups") Collection<BloodGroup> bloodGroups,
            @Param("city") String city,
            @Param("availability") AvailabilityStatus availability,
            @Param("requesterId") Long requesterId);

    List<DonorProfile> findAllByOrderByCreatedAtDesc();

    /**
     * Admin Donor Directory search (D-05). Each filter is ignored when
     * null. {@code query} matches name, email or phone (case-insensitive,
     * partial). Newest profiles first.
     */
    @Query(value = """
        SELECT d
        FROM DonorProfile d
        JOIN FETCH d.user u
        WHERE (:query IS NULL
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
               OR u.phone LIKE CONCAT('%', :query, '%'))
          AND (:bloodGroup IS NULL OR d.bloodGroup = :bloodGroup)
          AND (:city IS NULL OR LOWER(d.city) = LOWER(:city))
          AND (:availability IS NULL OR d.availability = :availability)
          AND (:active IS NULL OR d.active = :active)
        ORDER BY d.id DESC
        """,
        countQuery = """
        SELECT COUNT(d)
        FROM DonorProfile d
        JOIN d.user u
        WHERE (:query IS NULL
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
               OR u.phone LIKE CONCAT('%', :query, '%'))
          AND (:bloodGroup IS NULL OR d.bloodGroup = :bloodGroup)
          AND (:city IS NULL OR LOWER(d.city) = LOWER(:city))
          AND (:availability IS NULL OR d.availability = :availability)
          AND (:active IS NULL OR d.active = :active)
        """)
    Page<DonorProfile> searchDirectory(
            @Param("query") String query,
            @Param("bloodGroup") BloodGroup bloodGroup,
            @Param("city") String city,
            @Param("availability") AvailabilityStatus availability,
            @Param("active") Boolean active,
            Pageable pageable);

    /** Cities with at least one donor profile, for filters. */
    @Query("""
        SELECT DISTINCT d.city
        FROM DonorProfile d
        ORDER BY d.city
        """)
    List<String> findDistinctCities();

    /**
     * Anonymous availability counts (D-05): active, available donors with
     * an active account, grouped by blood group and city. Returns rows of
     * [BloodGroup, String city, Long count]. No identities are selected.
     */
    @Query("""
        SELECT d.bloodGroup, d.city, COUNT(d)
        FROM DonorProfile d
        JOIN d.user u
        WHERE d.active = true
          AND u.active = true
          AND d.availability = :available
        GROUP BY d.bloodGroup, d.city
        """)
    List<Object[]> countAvailableByBloodGroupAndCity(
            @Param("available") AvailabilityStatus available);

    // Admin dashboard counts (D-12)
    long countByActiveTrue();

    long countByActiveFalse();

    long countByActiveTrueAndAvailability(AvailabilityStatus availability);
}
