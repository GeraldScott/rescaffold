package io.archton.scaffold.util;

import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for handling errors in web routers.
 * Provides consistent error handling and user-friendly messages.
 */
public class WebErrorHandler {
    
    private static final Logger log = Logger.getLogger(WebErrorHandler.class);
    
    /**
     * Converts exceptions to user-friendly error messages.
     */
    public static String getUserFriendlyMessage(Exception e) {
        if (e instanceof DuplicateEntityException) {
            DuplicateEntityException dup = (DuplicateEntityException) e;
            return getDuplicateEntityMessage(dup);
        } else if (e instanceof EntityNotFoundException) {
            EntityNotFoundException notFound = (EntityNotFoundException) e;
            return String.format("The requested %s was not found.", notFound.getEntityType().toLowerCase());
        } else if (e instanceof ValidationException) {
            ValidationException val = (ValidationException) e;
            if (val.getErrors().size() == 1) {
                return val.getErrors().values().iterator().next();
            }
            return "Please correct the validation errors and try again.";
        } else if (e instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) e;
            Map<String, String> errors = getValidationErrors(cve);
            if (errors.size() == 1) {
                return errors.values().iterator().next();
            }
            return "Please correct the validation errors and try again.";
        } else if (e instanceof IllegalArgumentException) {
            return e.getMessage();
        } else {
            log.error("Unexpected error in web router", e);
            return "An unexpected error occurred. Please try again or contact support if the problem persists.";
        }
    }
    
    /**
     * Creates entity-specific duplicate error messages.
     */
    private static String getDuplicateEntityMessage(DuplicateEntityException dup) {
        String entityType = dup.getEntityType();
        String fieldName = dup.getFieldName();
        String operation = dup.getOperation();
        
        // Person-specific messages
        if ("Person".equals(entityType) && "email".equals(fieldName)) {
            if ("update".equals(operation)) {
                return "Another person already has this email address. Please use a different email.";
            } else {
                return "A person with this email address already exists. Please use a different email.";
            }
        }
        
        // Gender-specific messages
        if ("Gender".equals(entityType)) {
            if ("code".equals(fieldName)) {
                return "A gender with this code already exists. Please use a different code.";
            } else if ("description".equals(fieldName)) {
                return "A gender with this description already exists. Please use a different description.";
            }
        }
        
        // Title-specific messages
        if ("Title".equals(entityType)) {
            if ("code".equals(fieldName)) {
                return "A title with this code already exists. Please use a different code.";
            } else if ("description".equals(fieldName)) {
                return "A title with this description already exists. Please use a different description.";
            }
        }
        
        // IdType-specific messages
        if ("IdType".equals(entityType)) {
            if ("code".equals(fieldName)) {
                return "An ID type with this code already exists. Please use a different code.";
            } else if ("description".equals(fieldName)) {
                return "An ID type with this description already exists. Please use a different description.";
            }
        }
        
        // Generic fallback
        return String.format("A %s with %s '%s' already exists. Please use a different value.",
            entityType.toLowerCase(), fieldName, dup.getFieldValue());
    }
    
    /**
     * Extracts validation errors from a ConstraintViolationException.
     */
    public static Map<String, String> getValidationErrors(ConstraintViolationException e) {
        return e.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (existing, replacement) -> existing + "; " + replacement
            ));
    }
    
    /**
     * Creates a response for HTMX requests with error handling.
     */
    public static Response createErrorResponse(String html, Exception e) {
        if (e instanceof EntityNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).entity(html).build();
        } else if (e instanceof DuplicateEntityException) {
            // Return 200 with error message in HTML for better HTMX handling
            return Response.ok(html).build();
        } else if (e instanceof ValidationException || 
                   e instanceof ConstraintViolationException ||
                   e instanceof IllegalArgumentException) {
            // Return 200 with error message in HTML for better HTMX handling
            return Response.ok(html).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(html).build();
        }
    }
    
    /**
     * Logs the error with appropriate level based on exception type.
     */
    public static void logError(String context, Exception e) {
        if (e instanceof EntityNotFoundException || 
            e instanceof DuplicateEntityException ||
            e instanceof ValidationException ||
            e instanceof ConstraintViolationException ||
            e instanceof IllegalArgumentException) {
            log.warn(context + ": " + e.getMessage());
        } else {
            log.error(context, e);
        }
    }
}