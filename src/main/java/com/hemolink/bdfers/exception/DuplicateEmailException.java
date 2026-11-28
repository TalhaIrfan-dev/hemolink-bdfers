package com.hemolink.bdfers.exception;

/**
 * Thrown when a registration uses an email address that is already registered.
 * The registration page shows this message next to the email field (FR-01).
 */
public class DuplicateEmailException extends BusinessRuleException {

    public DuplicateEmailException() {
        super("This email address is already registered.");
    }
}
