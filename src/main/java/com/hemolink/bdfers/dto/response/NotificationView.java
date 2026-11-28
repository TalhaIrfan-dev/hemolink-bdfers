package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.enums.NotificationType;

import java.time.LocalDateTime;

/** One row on the notifications page (FR-13). */
public class NotificationView {

    private final Long id;
    private final NotificationType type;
    private final String message;
    private final boolean read;
    private final LocalDateTime createdAt;
    private final Long requestId;

    public NotificationView(Long id, NotificationType type, String message,
                            boolean read, LocalDateTime createdAt, Long requestId) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.requestId = requestId;
    }

    public Long getId() { return id; }
    public NotificationType getType() { return type; }
    public String getTypeLabel() { return type.getLabel(); }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** Related blood request, or null. */
    public Long getRequestId() { return requestId; }
}
