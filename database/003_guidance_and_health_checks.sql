-- =====================================================================
-- HemoLink (BDFERS) - Migration 003: Advisory eligibility (Module 2.3)
-- =====================================================================
-- Decisions D-01 to D-04 (eligibility is advisory only; guidance values
-- are stored in the database and edited by administrators).
--
-- Run ONCE on an existing hemolink_db, after database_schema.sql:
--     mysql -u root -p hemolink_db < database/003_guidance_and_health_checks.sql
--
-- NON-DESTRUCTIVE: only creates new tables and inserts default settings.
-- Nothing is dropped or altered. Safe to re-run (IF NOT EXISTS / INSERT IGNORE).
--
-- Note: donor_profiles.eligibility_status and eligibility_reason are kept
-- for history but are no longer used by the application.
-- =====================================================================

USE hemolink_db;

-- ---------------------------------------------------------------------
-- 1. DONATION GUIDANCE SETTINGS
--    One row per setting. setting_value NULL means the check is OFF.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS donation_guidance_settings (
    setting_key   VARCHAR(50)   PRIMARY KEY,
    setting_value DECIMAL(6,2)  NULL,
    updated_by    BIGINT        NULL,
    updated_at    DATETIME      NULL,

    CONSTRAINT fk_guidance_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Defaults (D-02). Interval, maximum units and CBC age are OFF (NULL):
-- those choices are left to the donor unless an administrator sets them.
-- Age, weight and haemoglobin defaults follow WHO public donor guidance.
INSERT IGNORE INTO donation_guidance_settings (setting_key, setting_value) VALUES
    ('MIN_HAEMOGLOBIN_G_DL',       12.50),
    ('MIN_WEIGHT_KG',              50.00),
    ('MIN_AGE_YEARS',              18.00),
    ('MAX_AGE_YEARS',              65.00),
    ('MIN_DAYS_BETWEEN_DONATIONS', NULL),
    ('MAX_UNITS_PER_DONATION',     NULL),
    ('MAX_CBC_AGE_DAYS',           NULL);

-- ---------------------------------------------------------------------
-- 2. GUIDANCE SETTINGS CHANGE HISTORY
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS guidance_settings_audit (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(50)   NOT NULL,
    old_value   DECIMAL(6,2)  NULL,
    new_value   DECIMAL(6,2)  NULL,
    changed_by  BIGINT        NOT NULL,
    changed_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_guidance_audit_setting
        FOREIGN KEY (setting_key) REFERENCES donation_guidance_settings(setting_key)
        ON DELETE RESTRICT,

    CONSTRAINT fk_guidance_audit_user
        FOREIGN KEY (changed_by) REFERENCES users(id)
        ON DELETE RESTRICT,

    INDEX idx_guidance_audit_changed (changed_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. DONOR HEALTH CHECKS (one per accepted match)
--    Never shown to requesters (NFR-03, D-04).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS donor_health_checks (
    id                      BIGINT        PRIMARY KEY AUTO_INCREMENT,
    match_id                BIGINT        NOT NULL UNIQUE,
    haemoglobin_g_dl        DECIMAL(4,1)  NOT NULL,
    cbc_date                DATE          NOT NULL,
    weight_kg               DECIMAL(5,2)  NOT NULL,
    units_offered           INT           NOT NULL,
    advice                  ENUM('OK', 'NOT_ADVISED') NOT NULL,
    advice_reasons          VARCHAR(1000) NULL,
    accepted_against_advice BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_health_check_match
        FOREIGN KEY (match_id) REFERENCES donor_matches(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_health_units
        CHECK (units_offered > 0),

    CONSTRAINT chk_health_haemoglobin
        CHECK (haemoglobin_g_dl > 0),

    CONSTRAINT chk_health_weight
        CHECK (weight_kg > 0),

    INDEX idx_health_check_advice (accepted_against_advice, created_at)
) ENGINE=InnoDB;

-- END OF MIGRATION 003
