package com.hemolink.bdfers.exception;

/**
 * Thrown when an operation is not allowed by a business rule, for example
 * an invalid request-status transition or a duplicate donor response.
 * The message is safe to show to the user.
 *
 * Extends IllegalStateException so that existing controller
 * catch blocks continue to work unchanged.
 */
public class BusinessRuleException extends IllegalStateException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
