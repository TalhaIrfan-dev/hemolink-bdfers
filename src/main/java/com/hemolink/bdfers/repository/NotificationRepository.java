
package com.hemolink.bdfers.repository;

import com.hemolink.bdfers.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserEmailOrderByCreatedAtDesc(
            String email);

    long countByUserEmailAndReadFalse(String email);
}
