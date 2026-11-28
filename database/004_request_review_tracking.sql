-- =====================================================================
-- HemoLink (BDFERS) - Migration 004: Request review and closure tracking
-- (Module 2.4)
-- =====================================================================
-- Records WHO reviewed (approved/rejected) or closed a blood request,
-- WHEN, and WHY it was closed (FR-16, D-13).
--
-- Run after 003:
--     mysql -u root -p hemolink_db < database/004_request_review_tracking.sql
--
-- NON-DESTRUCTIVE: only adds nullable columns, foreign keys and an index.
-- Existing requests keep their data; the new columns start empty.
-- SAFE TO RE-RUN: each change is skipped if it already exists.
-- =====================================================================

USE hemolink_db;

DROP PROCEDURE IF EXISTS hemolink_migration_004;

DELIMITER $$

CREATE PROCEDURE hemolink_migration_004()
BEGIN
    -- 1. Review and closure columns
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'blood_requests'
          AND COLUMN_NAME = 'reviewed_by'
    ) THEN
        ALTER TABLE blood_requests
            ADD COLUMN reviewed_by  BIGINT       NULL AFTER rejection_reason,
            ADD COLUMN reviewed_at  DATETIME     NULL AFTER reviewed_by,
            ADD COLUMN close_reason VARCHAR(255) NULL AFTER reviewed_at,
            ADD COLUMN closed_by    BIGINT       NULL AFTER close_reason,
            ADD COLUMN closed_at    DATETIME     NULL AFTER closed_by,
            ADD CONSTRAINT fk_request_reviewed_by
                FOREIGN KEY (reviewed_by) REFERENCES users(id)
                ON DELETE RESTRICT,
            ADD CONSTRAINT fk_request_closed_by
                FOREIGN KEY (closed_by) REFERENCES users(id)
                ON DELETE RESTRICT;
    END IF;

    -- 2. Index for the admin city filter (FR-16)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'blood_requests'
          AND INDEX_NAME = 'idx_request_city'
    ) THEN
        CREATE INDEX idx_request_city ON blood_requests(city);
    END IF;
END$$

DELIMITER ;

CALL hemolink_migration_004();

DROP PROCEDURE hemolink_migration_004;

-- END OF MIGRATION 004
