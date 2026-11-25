CREATE DATABASE hemolink_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

SHOW DATABASES;

USE hemolink_db;

-- 1. USER ACCOUNTS
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. DONOR PROFILES
CREATE TABLE donor_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    blood_group ENUM(
        'A_POSITIVE', 'A_NEGATIVE',
        'B_POSITIVE', 'B_NEGATIVE',
        'AB_POSITIVE', 'AB_NEGATIVE',
        'O_POSITIVE', 'O_NEGATIVE'
    ) NOT NULL,
    city VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    last_donation_date DATE NULL,
    availability ENUM('AVAILABLE', 'UNAVAILABLE')
        NOT NULL DEFAULT 'AVAILABLE',
    eligibility_status ENUM(
        'ELIGIBLE', 'INELIGIBLE', 'REQUIRES_REVIEW'
    ) NOT NULL DEFAULT 'REQUIRES_REVIEW',
    eligibility_reason VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_donor_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 3. BLOOD REQUESTS
CREATE TABLE blood_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    blood_group ENUM(
        'A_POSITIVE', 'A_NEGATIVE',
        'B_POSITIVE', 'B_NEGATIVE',
        'AB_POSITIVE', 'AB_NEGATIVE',
        'O_POSITIVE', 'O_NEGATIVE'
    ) NOT NULL,
    units_required INT NOT NULL,
    fulfilled_units INT NOT NULL DEFAULT 0,
    city VARCHAR(100) NOT NULL,
    hospital_name VARCHAR(150) NOT NULL,
    urgency ENUM('CRITICAL', 'NORMAL') NOT NULL,
    admin_status ENUM(
        'PENDING', 'APPROVED', 'REJECTED',
        'CLOSED', 'FULFILLED'
    ) NOT NULL DEFAULT 'PENDING',
    matching_status ENUM(
        'NOT_STARTED', 'IN_PROGRESS',
        'MATCHES_FOUND', 'NO_MATCHES'
    ) NOT NULL DEFAULT 'NOT_STARTED',
    rejection_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_request_requester
        FOREIGN KEY (requester_id) REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_units_required
        CHECK (units_required > 0),

    CONSTRAINT chk_fulfilled_units
        CHECK (
            fulfilled_units >= 0
            AND fulfilled_units <= units_required
        )
) ENGINE=InnoDB;

-- 4. DONOR MATCHES AND CONTACT DISCLOSURE
CREATE TABLE donor_matches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    donor_response ENUM(
        'PENDING', 'ACCEPTED', 'DECLINED'
    ) NOT NULL DEFAULT 'PENDING',
    donor_consent BOOLEAN NOT NULL DEFAULT FALSE,
    consent_at TIMESTAMP NULL,
    admin_approval ENUM(
        'PENDING', 'APPROVED', 'REJECTED'
    ) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT NULL,
    approval_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_request_donor
        UNIQUE (request_id, donor_id),

    CONSTRAINT fk_match_request
        FOREIGN KEY (request_id) REFERENCES blood_requests(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_match_donor
        FOREIGN KEY (donor_id) REFERENCES donor_profiles(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_match_approver
        FOREIGN KEY (approved_by) REFERENCES users(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 5. IN-APP NOTIFICATIONS
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    request_id BIGINT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_notification_request
        FOREIGN KEY (request_id) REFERENCES blood_requests(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 6. DONATION RECORDS
CREATE TABLE donation_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    match_id BIGINT NOT NULL UNIQUE,
    units_donated INT NOT NULL DEFAULT 0,
    outcome ENUM('COMPLETED', 'NOT_OCCURRED') NOT NULL,
    donation_date DATE NULL,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_donation_match
        FOREIGN KEY (match_id) REFERENCES donor_matches(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_units_donated
        CHECK (units_donated >= 0),

    CONSTRAINT chk_donation_outcome
        CHECK (
            (outcome = 'COMPLETED'
                AND units_donated > 0
                AND donation_date IS NOT NULL)
            OR
            (outcome = 'NOT_OCCURRED'
                AND units_donated = 0)
        )
) ENGINE=InnoDB;

-- INDEXES FOR COMMON SEARCHES
CREATE INDEX idx_donor_blood_city
    ON donor_profiles(blood_group, city);

CREATE INDEX idx_donor_availability
    ON donor_profiles(availability, eligibility_status, active);

CREATE INDEX idx_requester_requests
    ON blood_requests(requester_id, created_at);

CREATE INDEX idx_request_status
    ON blood_requests(admin_status, urgency, created_at);

CREATE INDEX idx_matches_request
    ON donor_matches(request_id);

CREATE INDEX idx_notifications_user
    ON notifications(user_id, is_read);

-- END OF SCHEMA

