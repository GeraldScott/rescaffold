package io.archton.scaffold.exception.mapper;

import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.exception.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic exception mapper for REST API endpoints.
 * Provides consistent error responses across the application.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger log = Logger.getLogger(GenericExceptionMapper.class);
    
    @Override
    public Response toResponse(Exception exception) {
        log.error("Exception caught by GenericExceptionMapper", exception);
        
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