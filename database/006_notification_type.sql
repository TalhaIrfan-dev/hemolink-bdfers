-- =====================================================================
-- HemoLink (BDFERS) - Migration 006: Notification type (Module 2.6)
-- =====================================================================
-- FR-13: adds a type to every in-app notification so the notifications
-- page can show an icon and filter by kind. Existing notifications get
-- the type GENERAL.
--
-- Run after 005:
--     mysql -u root -p hemolink_db < database/006_notification_type.sql
--
-- NON-DESTRUCTIVE and SAFE TO RE-RUN.
-- =====================================================================

USE hemolink_db;

DROP PROCEDURE IF EXISTS hemolink_migration_006;

DELIMITER $$

CREATE PROCEDURE hemolink_migration_006()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'notifications'
          AND COLUMN_NAME = 'type'
    ) THEN
        ALTER TABLE notifications
            ADD COLUMN type ENUM(
                'GENERAL', 'MATCH', 'RESPONSE', 'CONSENT', 'REVIEW',
                'REQUEST_UPDATE', 'DONATION', 'ACCOUNT', 'ADMIN_QUEUE'
            ) NOT NULL DEFAULT 'GENERAL' AFTER request_id;
    END IF;
END$$

DELIMITER ;

CALL hemolink_migration_006();

DROP PROCEDURE hemolink_migration_006;

-- END OF MIGRATION 006
