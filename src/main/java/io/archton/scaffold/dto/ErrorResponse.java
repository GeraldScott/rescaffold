package io.archton.scaffold.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    public String error;
    public String message;
    public LocalDateTime timestamp;
    public Map<String, String> violations;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error, String message) {
        this();
        this.error = sanitizeJson(error);
        this.message = sanitizeJson(message);
    }

    public static ErrorResponse fromConstraintViolation(ConstraintViolationException e) {
        ErrorResponse response = new ErrorResponse("Validation failed", "Request contains invalid data");

        response.violations = e.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> sanitizeJson(violation.getPropertyPath().toString()),
                violation -> sanitizeJson(violation.getMessage())
            ));

        return response;
    }

    public static ErrorResponse simple(String error, String message) {
        return new ErrorResponse(error, message);
    }

    public static ErrorResponse withException(String error, String baseMessage, Exception e) {
        String exceptionMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
        String fullMessage = baseMessage + ": " + exceptionMessage;
        return new ErrorResponse(error, fullMessage);
    }

    public static ErrorResponse withException(String error, String baseMessage, String detail, Exception e) {
        String exceptionMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
        String fullMessage = baseMessage + " " + detail + ": " + exceptionMessage;
        return new ErrorResponse(error, fullMessage);
    }

    /**
     * Sanitizes strings to prevent JSON parsing issues
     * - Replaces double quotes with single quotes
     * - Removes or replaces other problematic characters
     * - Handles null values
     */
    private static String sanitizeJson(String input) {
        if (input == null) {
            return null;
        }

        return input
            .replace("\"", "'")           // Replace double quotes with single quotes
            .replace("\n", " ")           // Replace newlines with spaces
            .replace("\r", " ")           // Replace carriage returns with spaces
            .replace("\t", " ")           // Replace tabs with spaces
            .replace("\\", "/")           // Replace backslashes with forward slashes
            .replaceAll("\\s+", " ")      // Replace multiple whitespace with single space
            .trim();                      // Remove leading/trailing whitespace
    }

    // Setters that automatically sanitize input
    public void setError(String error) {
        this.error = sanitizeJson(error);
    }

    public void setMessage(String message) {
        this.message = sanitizeJson(message);
    }

    public void setViolations(Map<String, String> violations) {
        if (violations != null) {
            this.violations = violations.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> sanitizeJson(entry.getKey()),
                    entry -> sanitizeJson(entry.getValue())
                ));
        } else {
            this.violations = null;
        }
    }
}