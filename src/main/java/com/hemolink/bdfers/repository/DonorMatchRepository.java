
package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonorMatch;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
