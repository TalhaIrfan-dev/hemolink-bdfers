
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

    @Query("""
        SELECT dr.match.id
        FROM DonationRecord dr
        WHERE dr.match.donor.user.email = :email
        """)
    List<Long> findReportedMatchIdsByDonorEmail(
            @Param("email") String email);
}
