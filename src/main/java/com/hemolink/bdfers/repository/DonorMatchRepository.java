
package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonorMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorMatchRepository
        extends JpaRepository<DonorMatch, Long> {

    List<DonorMatch> findAllByBloodRequestId(Long requestId);

    Optional<DonorMatch> findByBloodRequestIdAndDonorId(
            Long requestId, Long donorId);

    boolean existsByBloodRequestIdAndDonorId(
            Long requestId, Long donorId);
}
