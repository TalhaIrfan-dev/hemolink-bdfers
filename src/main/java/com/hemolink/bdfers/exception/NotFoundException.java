package com.hemolink.bdfers.exception;

/**
 * Thrown when a requested record does not exist or does not belong to
 * the current user. Shown to the user as a 404 page.
 *
 * Extends IllegalArgumentException so that existing controller
 * catch blocks continue to work unchanged.
 */
public class NotFoundException extends IllegalArgumentException {

    public NotFoundException(String message) {
        super(message);
    }
}
