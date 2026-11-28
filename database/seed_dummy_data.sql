-- =====================================================================
-- HemoLink (BDFERS) - Dummy seed data for development, testing and demos
-- =====================================================================
-- All names, email addresses and phone numbers are FICTIONAL (NFR-03).
-- Email addresses use the reserved ".test" domain.
--
-- Run AFTER database_schema.sql has been applied:
--     mysql -u root -p hemolink_db < database/seed_dummy_data.sql
--
-- Safe to run more than once: existing rows are not duplicated.
--
-- Login password for EVERY seeded account:   password
-- (BCrypt hash below is the example hash for "password" published in
--  the Spring Security reference documentation.)
--
-- Accounts:
--   admin@hemolink.test            Administrator
--   requester.hamza@hemolink.test  Requester (Sukkur)
--   requester.zainab@hemolink.test Requester (Karachi)
--   donor.*@hemolink.test          Donors (see below)
--
-- NOTE: eligibility_status values below are historical only. Eligibility
-- is advisory (decision D-01) and is no longer used for matching.
-- Run 003_guidance_and_health_checks.sql before starting the application.
-- =====================================================================

USE hemolink_db;

SET @pwd = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG';

-- ---------------------------------------------------------------------
-- 1. USERS
-- ---------------------------------------------------------------------
INSERT IGNORE INTO users (full_name, email, password, phone, role, active) VALUES
    ('HemoLink Administrator', 'talhairfanarain7861@gmail.com',  @pwd, '03000000000', 'ADMIN', TRUE),
    ('Hamza Qureshi',          'requester.hamza@hemolink.test',  @pwd, '03000000101', 'USER',  TRUE),
    ('Zainab Ansari',          'requester.zainab@hemolink.test', @pwd, '03000000102', 'USER',  TRUE),
    ('Ahmed Raza',             'donor.ahmed@hemolink.test',      @pwd, '03000000201', 'USER',  TRUE),
    ('Fatima Shah',            'donor.fatima@hemolink.test',     @pwd, '03000000202', 'USER',  TRUE),
    ('Bilal Memon',            'donor.bilal@hemolink.test',      @pwd, '03000000203', 'USER',  TRUE),
    ('Ayesha Siddiqui',        'donor.ayesha@hemolink.test',     @pwd, '03000000204', 'USER',  TRUE),
    ('Usman Ali',              'donor.usman@hemolink.test',      @pwd, '03000000205', 'USER',  TRUE),
    ('Sana Baloch',            'donor.sana@hemolink.test',       @pwd, '03000000206', 'USER',  TRUE);

-- ---------------------------------------------------------------------
-- 2. DONOR PROFILES
-- ---------------------------------------------------------------------
-- Ahmed: O- Sukkur, available, eligible
INSERT IGNORE INTO donor_profiles
    (user_id, blood_group, city, date_of_birth, weight, last_donation_date,
     availability, eligibility_status, eligibility_reason, active)
SELECT id, 'O_NEGATIVE', 'Sukkur', '1994-03-12', 72.50, NULL,
       'AVAILABLE', 'ELIGIBLE', 'Seed data: marked eligible for demonstration.', TRUE
FROM users WHERE email = 'donor.ahmed@hemolink.test';

-- Fatima: A+ Sukkur, available, eligible
INSERT IGNORE INTO donor_profiles
    (user_id, blood_group, city, date_of_birth, weight, last_donation_date,
     availability, eligibility_status, eligibility_reason, active)
SELECT id, 'A_POSITIVE', 'Sukkur', '1998-07-25', 58.00, '2026-05-10',
       'AVAILABLE', 'ELIGIBLE', 'Seed data: marked eligible for demonstration.', TRUE
FROM users WHERE email = 'donor.fatima@hemolink.test';

-- Bilal: B+ Karachi, available, eligible
INSERT IGNORE INTO donor_profiles
    (user_id, blood_group, city, date_of_birth, weight, last_donation_date,
     availability, eligibility_status, eligibility_reason, active)
SELECT id, 'B_POSITIVE', 'Karachi', '1990-11-02', 80.00, NULL,
       'AVAILABLE', 'ELIGIBLE', 'Seed data: marked eligible for demonstration.', TRUE
FROM users WHERE email = 'donor.bilal@hemolink.test';

-- Ayesha: AB+ Karachi, available, eligible
INSERT IGNORE INTO donor_profiles
    (user_id, blood_group, city, date_of_birth, weight, last_donation_date,
     availability, eligibility_status, eligibility_reason, active)
SELECT id, 'AB_POSITIVE', 'Karachi', '2000-01-18', 55.00, '2026-02-14',
       'AVAILABLE', 'ELIGIBLE', 'Seed data: marked eligible for demonstration.', TRUE
FROM users WHERE email = 'donor.ayesha@hemolink.test';

-- Usman: O+ Hyderabad, UNAVAILABLE (must not be matched - FR-05)
INSERT IGNORE INTO donor_profiles
    (user_id, blood_group, city, date_of_birth, weight, last_donation_date,
     availability, eligibility_status, eligibility_reason, active)
SELECT id, 'O_POSITIVE', 'Hyderabad', '1988-09-30', 85.00, NULL,
       'UNAVAILABLE', 'ELIGIBLE', 'Seed data: marked eligible for demonstration.', TRUE
FROM users WHERE email = 'donor.usman@hemolink.test';

-- Sana: A- Hyderabad, available, awaiting review
INSERT IGNORE INTO donor_profiles
    (user_id, blood_group, city, date_of_birth, weight, last_donation_date,
     availability, eligibility_status, eligibility_reason, active)
SELECT id, 'A_NEGATIVE', 'Hyderabad', '1996-04-05', 52.00, NULL,
       'AVAILABLE', 'REQUIRES_REVIEW', 'Eligibility criteria require review.', TRUE
FROM users WHERE email = 'donor.sana@hemolink.test';

-- ---------------------------------------------------------------------
-- 3. BLOOD REQUESTS
-- ---------------------------------------------------------------------
-- Normal, Pending: waits for admin approval before matching (FR-11)
INSERT INTO blood_requests
    (requester_id, blood_group, units_required, fulfilled_units, city,
     hospital_name, required_at, urgency, admin_status, matching_status)
SELECT u.id, 'A_POSITIVE', 2, 0, 'Sukkur',
       'Civil Hospital Sukkur', DATE_ADD(NOW(), INTERVAL 3 DAY),
       'NORMAL', 'PENDING', 'NOT_STARTED'
FROM users u
WHERE u.email = 'requester.hamza@hemolink.test'
  AND NOT EXISTS (
      SELECT 1 FROM blood_requests br
      WHERE br.requester_id = u.id
        AND br.hospital_name = 'Civil Hospital Sukkur'
        AND br.blood_group = 'A_POSITIVE');

-- Critical, Pending: requester can press "Refresh Matches" to match now
INSERT INTO blood_requests
    (requester_id, blood_group, units_required, fulfilled_units, city,
     hospital_name, required_at, urgency, admin_status, matching_status)
SELECT u.id, 'O_NEGATIVE', 1, 0, 'Sukkur',
       'Sukkur Blood and Diagnostic Centre', DATE_ADD(NOW(), INTERVAL 1 DAY),
       'CRITICAL', 'PENDING', 'NOT_STARTED'
FROM users u
WHERE u.email = 'requester.hamza@hemolink.test'
  AND NOT EXISTS (
      SELECT 1 FROM blood_requests br
      WHERE br.requester_id = u.id
        AND br.hospital_name = 'Sukkur Blood and Diagnostic Centre'
        AND br.blood_group = 'O_NEGATIVE');

-- Normal, Pending (Karachi)
INSERT INTO blood_requests
    (requester_id, blood_group, units_required, fulfilled_units, city,
     hospital_name, required_at, urgency, admin_status, matching_status)
SELECT u.id, 'B_POSITIVE', 1, 0, 'Karachi',
       'Jinnah Postgraduate Medical Centre', DATE_ADD(NOW(), INTERVAL 5 DAY),
       'NORMAL', 'PENDING', 'NOT_STARTED'
FROM users u
WHERE u.email = 'requester.zainab@hemolink.test'
  AND NOT EXISTS (
      SELECT 1 FROM blood_requests br
      WHERE br.requester_id = u.id
        AND br.hospital_name = 'Jinnah Postgraduate Medical Centre'
        AND br.blood_group = 'B_POSITIVE');

-- Closed request (history example)
INSERT INTO blood_requests
    (requester_id, blood_group, units_required, fulfilled_units, city,
     hospital_name, required_at, urgency, admin_status, matching_status)
SELECT u.id, 'AB_POSITIVE', 1, 0, 'Karachi',
       'Aga Khan University Hospital', DATE_ADD(NOW(), INTERVAL 2 DAY),
       'NORMAL', 'CLOSED', 'NOT_STARTED'
FROM users u
WHERE u.email = 'requester.zainab@hemolink.test'
  AND NOT EXISTS (
      SELECT 1 FROM blood_requests br
      WHERE br.requester_id = u.id
        AND br.hospital_name = 'Aga Khan University Hospital'
        AND br.blood_group = 'AB_POSITIVE');

-- ---------------------------------------------------------------------
-- FALLBACK (only if logging in with "password" fails):
--   1. Register admin2@hemolink.test through the application.
--   2. Run the following line:
-- UPDATE users SET role = 'ADMIN' WHERE email = 'admin2@hemolink.test';
-- ---------------------------------------------------------------------

-- END OF SEED DATA
