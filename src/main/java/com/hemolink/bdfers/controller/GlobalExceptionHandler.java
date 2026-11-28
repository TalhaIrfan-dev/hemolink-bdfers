package com.hemolink.bdfers.controller;

import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.net.URI;

/**
 * Converts exceptions that escape the controllers into user-friendly
 * responses, so users never see a stack trace or a raw HTTP 500 page
 * (NFR-04).
 *
 * <ul>
 *   <li>{@link NotFoundException} - 404 page.</li>
 *   <li>{@link BusinessRuleException} on a form submission - redirect back
 *       to the previous page with the message shown as an alert.</li>
 *   <li>Invalid parameters - 400 page.</li>
 *   <li>Anything unexpected - logged, and a generic 500 page without
 *       technical details.</li>
 * </ul>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String FALLBACK_REDIRECT = "/dashboard";

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFound(NotFoundException ex) {
        return errorPage(404,
                "Not found",
                ex.getMessage() + " It may not exist, or you may not have access to it.");
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ModelAndView handleBusinessRule(
            BusinessRuleException ex,
            HttpServletRequest request) {

        // For page views, show the message on an error page to avoid
        // a redirect loop back to the same page.
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return errorPage(409, "Action not allowed", ex.getMessage());
        }

        // For form submissions, return the user to the page they came from.
        RequestContextUtils.getOutputFlashMap(request)
                .put("errorMessage", ex.getMessage());

        return new ModelAndView("redirect:" + safeRedirectTarget(request));
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ModelAndView handleTypeMismatch(TypeMismatchException ex) {
        return errorPage(400,
                "Invalid request",
                "The request contained an invalid value. Please go back and try again.");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception ex) throws Exception {

        // Let Spring Security handle its own exceptions (login redirect, 403).
        if (ex instanceof AccessDeniedException
                || ex instanceof AuthenticationException) {
            throw ex;
        }

        // Standard Spring MVC errors (e.g. unknown URL, wrong HTTP method)
        // already carry the correct status code.
        if (ex instanceof ErrorResponse errorResponse) {
            int status = errorResponse.getStatusCode().value();
            if (status == 404) {
                return errorPage(404, "Page not found",
                        "The page you are looking for does not exist.");
            }
            return errorPage(status, "Request could not be processed",
                    "Please go back and try again.");
        }

        log.error("Unexpected error", ex);

        return errorPage(500,
                "Something went wrong",
                "An unexpected error occurred. No changes were saved. "
                + "Please try again, or contact an administrator if the problem continues.");
    }

    private ModelAndView errorPage(int status, String title, String detail) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatusCode.valueOf(status));
        mav.addObject("status", status);
        mav.addObject("errorTitle", title);
        mav.addObject("errorDetail", detail);
        return mav;
    }

    /**
     * Returns the path of the previous page (from the Referer header) if it
     * belongs to this application; otherwise the dashboard. Prevents open
     * redirects to external sites.
     */
    private String safeRedirectTarget(HttpServletRequest request) {
        String referer = request.getHeader("Referer");

        if (referer == null || referer.isBlank()) {
            return FALLBACK_REDIRECT;
        }

        try {
            URI uri = URI.create(referer);

            if (uri.getHost() != null
                    && !uri.getHost().equalsIgnoreCase(request.getServerName())) {
                return FALLBACK_REDIRECT;
            }

            String path = uri.getRawPath();

            if (path == null || !path.startsWith("/") || path.startsWith("//")) {
                return FALLBACK_REDIRECT;
            }

            // "redirect:" adds the context path itself, so remove it here.
            String contextPath = request.getContextPath();
            if (!contextPath.isEmpty() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }

            if (path.isEmpty()) {
                path = "/";
            }

            String query = uri.getRawQuery();
            return query == null ? path : path + "?" + query;

        } catch (IllegalArgumentException ex) {
            return FALLBACK_REDIRECT;
        }
    }
}
