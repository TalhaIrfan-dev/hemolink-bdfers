-- =====================================================================
-- HemoLink (BDFERS) - Migration 007: Indexes for dashboard and log queries
-- (Phase 4 database review)
-- =====================================================================
-- Adds three secondary indexes that match queries added in Modules 2.2-2.7:
--   1. idx_request_created     blood_requests(created_at)
--        Dashboard trend charts and "+N this week" counts
--        (BloodRequestRepository.findCreatedAtBetween, countBy...CreatedAt...).
--   2. idx_matches_created     donor_matches(created_at)
--        Matches line on the trend charts (DonorMatchRepository.findCreatedAtBetween).
--   3. idx_matches_disclosure  donor_matches(donor_response, donor_consent, consent_at)
--        Admin disclosure log (DonorMatchRepository.findDisclosureLog).
--
-- Run after 006:
--     mysql -u root -p hemolink_db < database/007_query_indexes.sql
--
-- NON-DESTRUCTIVE: only adds indexes. No table, column or row is changed
-- or removed. SAFE TO RE-RUN: each index is skipped if it already exists.
-- Optional: the application starts without it (indexes are not checked
-- by ddl-auto=validate); it only makes these queries faster.
-- =====================================================================

USE hemolink_db;

DROP PROCEDURE IF EXISTS hemolink_migration_007;

DELIMITER $$

CREATE PROCEDURE hemolink_migration_007()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'blood_requests'
          AND INDEX_NAME = 'idx_request_created'
    ) THEN
        CREATE INDEX idx_request_created ON blood_requests(created_at);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'donor_matches'
          AND INDEX_NAME = 'idx_matches_created'
    ) THEN
        CREATE INDEX idx_matches_created ON donor_matches(created_at);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'donor_matches'
          AND INDEX_NAME = 'idx_matches_disclosure'
    ) THEN
        CREATE INDEX idx_matches_disclosure
            ON donor_matches(donor_response, donor_consent, consent_at);
    END IF;
END$$

DELIMITER ;

CALL hemolink_migration_007();

DROP PROCEDURE hemolink_migration_007;

-- END OF MIGRATION 007
