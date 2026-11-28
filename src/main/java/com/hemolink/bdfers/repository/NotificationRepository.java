package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserEmailOrderByCreatedAtDesc(
            String email);

    long countByUserEmailAndReadFalse(String email);

    /** Latest notifications for the dashboard "Recent Activity" panel. */
    @EntityGraph(attributePaths = "bloodRequest")
    List<Notification> findTop5ByUserEmailOrderByIdDesc(String email);

    /** Notifications page: all of the user's notifications, newest first. */
    @EntityGraph(attributePaths = "bloodRequest")
    Page<Notification> findAllByUserEmailOrderByIdDesc(
            String email, Pageable pageable);

    /** Notifications page: unread only, newest first. */
    @EntityGraph(attributePaths = "bloodRequest")
    Page<Notification> findAllByUserEmailAndReadFalseOrderByIdDesc(
            String email, Pageable pageable);

    /** A notification only if it belongs to this user (FR-03). */
    @EntityGraph(attributePaths = "bloodRequest")
    Optional<Notification> findByIdAndUserEmail(Long id, String email);

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.read = true
        WHERE n.user.id = :userId
          AND n.read = false
        """)
    int markAllRead(@Param("userId") Long userId);
}
