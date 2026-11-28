package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.DonorStatusChange;
import com.hemolink.bdfers.enums.DonorStatusAction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonorStatusChangeRepository
        extends JpaRepository<DonorStatusChange, Long> {

    /** Full history for one donor, newest first, with the admin loaded. */
    @EntityGraph(attributePaths = "changedBy")
    List<DonorStatusChange> findAllByDonor_IdOrderByIdDesc(Long donorId);

    /** Most recent change of a given kind (used for the donor's notice). */
    Optional<DonorStatusChange> findFirstByDonor_IdAndActionOrderByIdDesc(
            Long donorId, DonorStatusAction action);

    /** Latest changes across all donors (admin recent activity). */
    @EntityGraph(attributePaths = {"changedBy", "donor", "donor.user"})
    List<DonorStatusChange> findTop5ByOrderByIdDesc();
}
