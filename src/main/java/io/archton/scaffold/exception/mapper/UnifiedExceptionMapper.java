package io.archton.scaffold.exception.mapper;

import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.exception.ValidationException;
import io.quarkus.qute.Template;
import io.quarkus.qute.Location;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Unified exception mapper that handles both JSON and HTML responses
 * based on the request's Accept header. This mapper should primarily handle
 * API requests and navigation errors, as form submissions are handled by
 * the BaseEntityRouter in each web controller.
 */
@Provider
public class UnifiedExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger log = Logger.getLogger(UnifiedExceptionMapper.class);
    
    @Context
    HttpHeaders headers;
    
    @Inject
    @Location("components/error-alert")
    Template errorAlertTemplate;
    
    @Override
    public Response toResponse(Exception exception) {
        // Log for debugging purposes
        log.debugf("UnifiedExceptionMapper handling exception: %s", exception.getClass().getSimpleName());
        
        // Determine if this is an HTML request
        if (acceptsHtml()) {
            return handleHtmlError(exception);
        } else {
            return handleJsonError(exception);
        }
    }
    
    private boolean acceptsHtml() {
        if (headers == null) {
            // Fallback to JSON if headers not available
            return false;
        }
        
        // Check Accept header to determine response type
        String acceptHeader = headers.getHeaderString(HttpHeaders.ACCEPT);
        if (acceptHeader == null) {
            return false;
        }
        
        // Check if client accepts HTML more than JSON
        return acceptHeader.contains(MediaType.TEXT_HTML) && 
               (!acceptHeader.contains(MediaType.APPLICATION_JSON) || 
                acceptHeader.indexOf(MediaType.TEXT_HTML) < acceptHeader.indexOf(MediaType.APPLICATION_JSON));
    }
    
    /**
     * Handle errors for HTML/HTMX requests.
     * Note: Form submissions should be handled by BaseEntityRouter in the web controllers.
     * This method primarily handles navigation errors and other non-form exceptions.
     */
    private Response handleHtmlError(Exception exception) {
        logError("HTML error (non-form)", exception);
        
        String userMessage = getUserFriendlyMessage(exception);
        String html = errorAlertTemplate.data("errorMessage", userMessage).render();
        
        Response.Status status = getHttpStatus(exception);
        
        // For validation errors, return 200 for better HTMX handling
        if (exception instanceof ValidationException || 
            exception instanceof DuplicateEntityException ||
            exception instanceof IllegalArgumentException) {
            return Response.ok(html).build();
        }
        
        return Response.status(status).entity(html).build();
    }
    
    /**
     * Handle errors for JSON API requests  
     */
    private Response handleJsonError(Exception exception) {
        logError("JSON API error", exception);
        
        if (exception instanceof EntityNotFoundException) {
            return handleEntityNotFoundException((EntityNotFoundException) exception);
        } else if (exception instanceof DuplicateEntityException) {
            return handleDuplicateEntityException((DuplicateEntityException) exception);
        } else if (exception instanceof ValidationException) {
            return handleValidationException((ValidationException) exception);
        } else if (exception instanceof IllegalArgumentException) {
            return handleIllegalArgumentException((IllegalArgumentException) exception);
        } else {
            return handleGenericException(exception);
        }
    }
    
    /**
     * Generate user-friendly error messages
     */
    private String getUserFriendlyMessage(Exception e) {
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
        } else if (e instanceof IllegalArgumentException) {
            return e.getMessage();
        } else {
            return "An unexpected error occurred. Please try again or contact support if the problem persists.";
        }
    }
    
    /**
     * Generate generic duplicate entity message (removes hard-coded entity logic)
     */
    private String getDuplicateEntityMessage(DuplicateEntityException dup) {
        String entityType = dup.getEntityType();
        String fieldName = dup.getFieldName();
        String fieldValue = String.valueOf(dup.getFieldValue());
        String operation = dup.getOperation();
        
        // Use generic message format for all entities
        if ("update".equals(operation)) {
            return String.format("Another %s already has this %s (%s). Please use a different value.",
                entityType.toLowerCase(), fieldName, fieldValue);
        } else {
            return String.format("A %s with %s '%s' already exists. Please use a different value.",
                entityType.toLowerCase(), fieldName, fieldValue);
        }
    }
    
    
    /**
     * Get appropriate HTTP status for exception type
     */
    private Response.Status getHttpStatus(Exception exception) {
        if (exception instanceof EntityNotFoundException) {
            return Response.Status.NOT_FOUND;
        } else if (exception instanceof DuplicateEntityException) {
            return Response.Status.CONFLICT;
        } else if (exception instanceof ValidationException || 
                   exception instanceof IllegalArgumentException) {
            return Response.Status.BAD_REQUEST;
        } else {
            return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * Log errors with appropriate level
     */
    private void logError(String context, Exception e) {
        if (e instanceof EntityNotFoundException || 
            e instanceof DuplicateEntityException ||
            e instanceof ValidationException ||
            e instanceof IllegalArgumentException) {
            log.warn(context + ": " + e.getMessage());
        } else {
            log.error(context, e);
        }
    }
    
    // JSON Response Handlers (from original GenericExceptionMapper)
    
    private Response handleEntityNotFoundException(EntityNotFoundException e) {
        Map<String, Object> errorResponse = createErrorResponse(
            "ENTITY_NOT_FOUND",
            e.getMessage(),
            Response.Status.NOT_FOUND
        );
        errorResponse.put("entityType", e.getEntityType());
        errorResponse.put("id", e.getId());
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity(errorResponse)
            .build();
    }
    
    private Response handleDuplicateEntityException(DuplicateEntityException e) {
        Map<String, Object> errorResponse = createErrorResponse(
            "DUPLICATE_ENTITY",
            e.getMessage(),
            Response.Status.CONFLICT
        );
        errorResponse.put("entityType", e.getEntityType());
        errorResponse.put("fieldName", e.getFieldName());
        errorResponse.put("fieldValue", e.getFieldValue());
        errorResponse.put("operation", e.getOperation());
        
        return Response.status(Response.Status.CONFLICT)
            .entity(errorResponse)
            .build();
    }
    
    private Response handleValidationException(ValidationException e) {
        Map<String, Object> errorResponse = createErrorResponse(
            "VALIDATION_ERROR",
            e.getMessage(),
            Response.Status.BAD_REQUEST
        );
        errorResponse.put("errors", e.getErrors());
        
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .build();
    }
    
    
    private Response handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, Object> errorResponse = createErrorResponse(
            "INVALID_ARGUMENT",
            e.getMessage(),
            Response.Status.BAD_REQUEST
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .build();
    }
    
    private Response handleGenericException(Exception e) {
        Map<String, Object> errorResponse = createErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            Response.Status.INTERNAL_SERVER_ERROR
        );
        
        // In development, include the actual error message
        String profile = System.getProperty("quarkus.profile", "prod");
        if ("dev".equals(profile) || "test".equals(profile)) {
            errorResponse.put("debugMessage", e.getMessage());
            errorResponse.put("exceptionType", e.getClass().getSimpleName());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(errorResponse)
            .build();
    }
    
    private Map<String, Object> createErrorResponse(String code, String message, Response.Status status) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("status", status.getStatusCode());
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}