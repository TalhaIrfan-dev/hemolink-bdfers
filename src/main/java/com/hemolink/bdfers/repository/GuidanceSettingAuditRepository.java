package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.GuidanceSettingAudit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuidanceSettingAuditRepository
        extends JpaRepository<GuidanceSettingAudit, Long> {

    /** The 20 most recent changes, with the administrator loaded. */
    @EntityGraph(attributePaths = "changedBy")
    List<GuidanceSettingAudit> findTop20ByOrderByIdDesc();
}
