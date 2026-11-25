
package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.enums.RequestAdminStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT br
        FROM BloodRequest br
        WHERE br.id = :requestId
        """)
    Optional<BloodRequest> findByIdForUpdate(
            @Param("requestId") Long requestId);
}
