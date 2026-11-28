package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Active accounts with a role, e.g. all administrators (FR-13 admin queue). */
    List<AppUser> findAllByRoleAndActiveTrue(UserRole role);
}