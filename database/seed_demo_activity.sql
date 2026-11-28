-- =====================================================================
-- HemoLink (BDFERS) - OPTIONAL demo activity (Phase 4)
-- =====================================================================
-- Adds realistic, FICTIONAL activity on top of seed_dummy_data.sql so that
-- every page has something to show in a demo or a manual test:
--
--   A. REQ for O- at Sukkur Blood and Diagnostic Centre (critical, from the
--      base seed): matched to donor Ahmed (O-, Sukkur), who ACCEPTED with a
--      health check that "Looks OK" and SHARED his contact details.
--      -> requester Hamza sees Ahmed on Donor Contacts; disclosure log "Visible".
--
--   B. NEW request for B+ at National Institute of Blood Disease, Karachi
--      (Normal, requester Zainab): approved by the admin, matched to donor
--      Bilal (B+, Karachi), accepted AGAINST ADVICE (haemoglobin below the
--      guidance level), consented, donated 1 unit -> FULFILLED.
--      -> donation history, "accepted against advice (30 days)" card,
--         disclosure log "Hidden: request fulfilled", admin activity.
--
--   C. Donor Sana (A-, Hyderabad): deactivated and later reactivated by the
--      admin, with reasons. She stays ACTIVE at the end.
--      -> deactivation history on her admin page; admin activity.
--
--   D. Closed request at Aga Khan University Hospital (from the base seed):
--      gets the close reason, who closed it and when, if those are empty.
--
--   E. In-app notifications for the people involved.
--
-- Run AFTER database_schema.sql, 003-007 and seed_dummy_data.sql:
--     mysql -u root -p hemolink_db < database/seed_demo_activity.sql
--
-- SAFE TO RE-RUN: every insert checks for an existing row, and every update
-- only fills values that are still empty / in their starting state.
-- It never deletes anything and never changes rows you created yourself
-- (it only touches the seeded .test accounts and their seeded requests).
-- Dates are relative to NOW() so "this week" counts and charts show data.
-- =====================================================================

USE hemolink_db;

-- ---------------------------------------------------------------------
-- 0. Look up the seeded accounts and requests (NULL if the base seed is missing)
-- ---------------------------------------------------------------------
SET @admin  = (SELECT id FROM users WHERE email = 'admin@hemolink.test');
SET @hamza  = (SELECT id FROM users WHERE email = 'requester.hamza@hemolink.test');
SET @zainab = (SELECT id FROM users WHERE email = 'requester.zainab@hemolink.test');

SET @ahmed_user = (SELECT id FROM users WHERE email = 'donor.ahmed@hemolink.test');
SET @bilal_user = (SELECT id FROM users WHERE email = 'donor.bilal@hemolink.test');
SET @ahmed = (SELECT id FROM donor_profiles WHERE user_id = @ahmed_user);
SET @bilal = (SELECT id FROM donor_profiles WHERE user_id = @bilal_user);
SET @sana  = (SELECT dp.id FROM donor_profiles dp JOIN users u ON u.id = dp.user_id
              WHERE u.email = 'donor.sana@hemolink.test');

SET @req_a = (SELECT id FROM blood_requests
              WHERE requester_id = @hamza
                AND hospital_name = 'Sukkur Blood and Diagnostic Centre'
                AND blood_group = 'O_NEGATIVE'
              ORDER BY id LIMIT 1);

SET @req_closed = (SELECT id FROM blood_requests
                   WHERE requester_id = @zainab
                     AND hospital_name = 'Aga Khan University Hospital'
                     AND blood_group = 'AB_POSITIVE'
                   ORDER BY id LIMIT 1);

-- ---------------------------------------------------------------------
-- A. Critical O- request: Ahmed accepts and shares his contact details
-- ---------------------------------------------------------------------
INSERT IGNORE INTO donor_matches
    (request_id, donor_id, donor_response, donor_consent, consent_at, created_at)
SELECT @req_a, @ahmed, 'ACCEPTED', TRUE, NOW() - INTERVAL 50 MINUTE, NOW() - INTERVAL 3 HOUR
FROM DUAL
WHERE @req_a IS NOT NULL AND @ahmed IS NOT NULL;

UPDATE blood_requests
SET matching_status = 'MATCHES_FOUND'
WHERE id = @req_a AND matching_status = 'NOT_STARTED';

SET @match_a = (SELECT id FROM donor_matches WHERE request_id = @req_a AND donor_id = @ahmed);

INSERT IGNORE INTO donor_health_checks
    (match_id, haemoglobin_g_dl, cbc_date, weight_kg, units_offered,
     advice, advice_reasons, accepted_against_advice, created_at)
SELECT @match_a, 14.2, CURDATE() - INTERVAL 5 DAY, 72.50, 1,
       'OK', NULL, FALSE, NOW() - INTERVAL 1 HOUR
FROM DUAL
WHERE @match_a IS NOT NULL;

-- ---------------------------------------------------------------------
-- B. Normal B+ request: approved, accepted against advice, donated, fulfilled
-- ---------------------------------------------------------------------
INSERT INTO blood_requests
    (requester_id, blood_group, units_required, fulfilled_units, city,
     hospital_name, required_at, urgency, admin_status, matching_status,
     reviewed_by, reviewed_at, created_at)
SELECT @zainab, 'B_POSITIVE', 1, 0, 'Karachi',
       'National Institute of Blood Disease', NOW() - INTERVAL 4 DAY,
       'NORMAL', 'APPROVED', 'MATCHES_FOUND',
       @admin, NOW() - INTERVAL 6 DAY + INTERVAL 2 HOUR, NOW() - INTERVAL 6 DAY
FROM DUAL
WHERE @zainab IS NOT NULL AND @admin IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM blood_requests br
      WHERE br.requester_id = @zainab
        AND br.hospital_name = 'National Institute of Blood Disease'
        AND br.blood_group = 'B_POSITIVE');

SET @req_b = (SELECT id FROM blood_requests
              WHERE requester_id = @zainab
                AND hospital_name = 'National Institute of Blood Disease'
                AND blood_group = 'B_POSITIVE'
              ORDER BY id LIMIT 1);

INSERT IGNORE INTO donor_matches
    (request_id, donor_id, donor_response, donor_consent, consent_at, created_at)
SELECT @req_b, @bilal, 'ACCEPTED', TRUE,
       NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 6 DAY + INTERVAL 2 HOUR
FROM DUAL
WHERE @req_b IS NOT NULL AND @bilal IS NOT NULL;

SET @match_b = (SELECT id FROM donor_matches WHERE request_id = @req_b AND donor_id = @bilal);

INSERT IGNORE INTO donor_health_checks
    (match_id, haemoglobin_g_dl, cbc_date, weight_kg, units_offered,
     advice, advice_reasons, accepted_against_advice, created_at)
SELECT @match_b, 12.1, CURDATE() - INTERVAL 8 DAY, 80.00, 1,
       'NOT_ADVISED',
       'Haemoglobin 12.1 g/dL is below the guidance level of 12.5 g/dL.',
       TRUE, NOW() - INTERVAL 5 DAY
FROM DUAL
WHERE @match_b IS NOT NULL;

INSERT IGNORE INTO donation_records
    (match_id, units_donated, outcome, donation_date, reported_at)
SELECT @match_b, 1, 'COMPLETED', CURDATE() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY
FROM DUAL
WHERE @match_b IS NOT NULL;

-- Same effect as reporting the donation in the app: units counted,
-- request fulfilled, donor's last donation date updated.
UPDATE blood_requests
SET fulfilled_units = 1, admin_status = 'FULFILLED'
WHERE id = @req_b
  AND admin_status = 'APPROVED'
  AND EXISTS (SELECT 1 FROM donation_records WHERE match_id = @match_b AND outcome = 'COMPLETED');

UPDATE donor_profiles
SET last_donation_date = CURDATE() - INTERVAL 4 DAY
WHERE id = @bilal
  AND (last_donation_date IS NULL OR last_donation_date < CURDATE() - INTERVAL 4 DAY)
  AND EXISTS (SELECT 1 FROM donation_records WHERE match_id = @match_b AND outcome = 'COMPLETED');

-- ---------------------------------------------------------------------
-- C. Sana: deactivated, then reactivated (she ends up ACTIVE)
-- ---------------------------------------------------------------------
INSERT INTO donor_status_changes (donor_id, action, reason, changed_by, changed_at)
SELECT @sana, 'DEACTIVATED',
       'Phone number could not be reached. Please confirm your contact details.',
       @admin, NOW() - INTERVAL 10 DAY
FROM DUAL
WHERE @sana IS NOT NULL AND @admin IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM donor_status_changes
                  WHERE donor_id = @sana AND action = 'DEACTIVATED');

INSERT INTO donor_status_changes (donor_id, action, reason, changed_by, changed_at)
SELECT @sana, 'REACTIVATED', 'Contact details confirmed by phone.',
       @admin, NOW() - INTERVAL 9 DAY
FROM DUAL
WHERE @sana IS NOT NULL AND @admin IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM donor_status_changes
                  WHERE donor_id = @sana AND action = 'REACTIVATED');

-- ---------------------------------------------------------------------
-- D. Closed request: record who closed it and why (only if still empty)
-- ---------------------------------------------------------------------
UPDATE blood_requests
SET close_reason = 'Blood was arranged through the hospital blood bank.',
    closed_by = @zainab,
    closed_at = NOW() - INTERVAL 2 DAY
WHERE id = @req_closed
  AND admin_status = 'CLOSED'
  AND close_reason IS NULL;

-- ---------------------------------------------------------------------
-- E. Notifications (wording follows NotificationService)
-- ---------------------------------------------------------------------
-- Helper values
SET @id_a = CONCAT('REQ-', IF(@req_a < 10000, LPAD(@req_a, 4, '0'), @req_a));  -- same as REQ-%04d
SET @id_b = CONCAT('REQ-', IF(@req_b < 10000, LPAD(@req_b, 4, '0'), @req_b));

-- Ahmed: matched to the critical O- request
INSERT INTO notifications (user_id, request_id, type, message, is_read, created_at)
SELECT @ahmed_user, @req_a, 'MATCH',
       CONCAT('You have been matched to critical blood request ', @id_a,
              ' (O-, 1 unit(s), Sukkur) at Sukkur Blood and Diagnostic Centre.'),
       TRUE, NOW() - INTERVAL 3 HOUR
FROM DUAL
WHERE @req_a IS NOT NULL AND @ahmed_user IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = @ahmed_user AND request_id = @req_a AND type = 'MATCH');

-- Hamza: Ahmed accepted and shared contact details (unread)
INSERT INTO notifications (user_id, request_id, type, message, is_read, created_at)
SELECT @hamza, @req_a, 'CONSENT',
       CONCAT('A donor accepted your blood request ', @id_a,
              ' and shared their contact details. Open Donor Contacts to see them.'),
       FALSE, NOW() - INTERVAL 50 MINUTE
FROM DUAL
WHERE @req_a IS NOT NULL AND @hamza IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = @hamza AND request_id = @req_a AND type = 'CONSENT');

-- Admin: new requests in the queue
INSERT INTO notifications (user_id, request_id, type, message, is_read, created_at)
SELECT @admin, @req_b, 'ADMIN_QUEUE',
       CONCAT('New blood request ', @id_b, ' (B+, 1 unit(s), Karachi) is awaiting your approval.'),
       TRUE, NOW() - INTERVAL 6 DAY
FROM DUAL
WHERE @req_b IS NOT NULL AND @admin IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = @admin AND request_id = @req_b AND type = 'ADMIN_QUEUE');

-- Zainab: approved, then fulfilled
INSERT INTO notifications (user_id, request_id, type, message, is_read, created_at)
SELECT @zainab, @req_b, 'REVIEW',
       CONCAT('Your blood request ', @id_b, ' has been approved. Matching donors are being notified.'),
       TRUE, NOW() - INTERVAL 6 DAY + INTERVAL 2 HOUR
FROM DUAL
WHERE @req_b IS NOT NULL AND @zainab IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = @zainab AND request_id = @req_b AND type = 'REVIEW');

INSERT INTO notifications (user_id, request_id, type, message, is_read, created_at)
SELECT @zainab, @req_b, 'DONATION',
       CONCAT('A donor reported donating 1 unit(s) for your blood request ', @id_b,
              '. All required units have been donated and the request is fulfilled.'),
       FALSE, NOW() - INTERVAL 4 DAY
FROM DUAL
WHERE @req_b IS NOT NULL AND @zainab IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = @zainab AND request_id = @req_b AND type = 'DONATION');

-- Bilal: matched to the B+ request
INSERT INTO notifications (user_id, request_id, type, message, is_read, created_at)
SELECT @bilal_user, @req_b, 'MATCH',
       CONCAT('You have been matched to blood request ', @id_b,
              ' (B+, 1 unit(s), Karachi) at National Institute of Blood Disease.'),
       TRUE, NOW() - INTERVAL 6 DAY + INTERVAL 2 HOUR
FROM DUAL
WHERE @req_b IS NOT NULL AND @bilal_user IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = @bilal_user AND request_id = @req_b AND type = 'MATCH');

-- Sana: account notices for the deactivation and reactivation
INSERT INTO notifications (user_id, request_id, type, message, is_read, created_at)
SELECT dp.user_id, NULL, 'ACCOUNT',
       'Your donor profile was reactivated by an administrator. You can be matched to requests again.',
       TRUE, NOW() - INTERVAL 9 DAY
FROM donor_profiles dp
WHERE dp.id = @sana
  AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id = dp.user_id AND n.type = 'ACCOUNT');

-- END OF DEMO ACTIVITY
