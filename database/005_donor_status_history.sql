-- =====================================================================
-- HemoLink (BDFERS) - Migration 005: Donor deactivation history (Module 2.5)
-- =====================================================================
-- FR-17: administrators deactivate or reactivate donor profiles with a
-- recorded reason. Every change is kept (who, when, why), not only the
-- latest one.
--
-- Run after 004:
--     mysql -u root -p hemolink_db < database/005_donor_status_history.sql
--
-- NON-DESTRUCTIVE: creates one new table. SAFE TO RE-RUN.
-- The current state stays in donor_profiles.active (already exists).
-- =====================================================================

USE hemolink_db;

CREATE TABLE IF NOT EXISTS donor_status_changes (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    donor_id    BIGINT       NOT NULL,
    action      ENUM('DEACTIVATED', 'REACTIVATED') NOT NULL,
    reason      VARCHAR(255) NOT NULL,
    changed_by  BIGINT       NOT NULL,
    changed_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_status_change_donor
        FOREIGN KEY (donor_id) REFERENCES donor_profiles(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_status_change_admin
        FOREIGN KEY (changed_by) REFERENCES users(id)
        ON DELETE RESTRICT,

    INDEX idx_status_change_donor (donor_id, changed_at)
) ENGINE=InnoDB;

-- END OF MIGRATION 005
