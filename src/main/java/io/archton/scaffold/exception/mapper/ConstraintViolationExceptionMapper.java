package io.archton.scaffold.exception.mapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exception mapper specifically for Bean Validation constraint violations.
 * Returns validation error messages in the format expected by tests.
 */
@Provider  
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    
    private static final Logger log = Logger.getLogger(ConstraintViolationExceptionMapper.class);
    
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        log.errorf("Constraint violation exception caught: %s", exception.getClass().getName());
        
        // Get the first violation message for simple error response
        String errorMessage = exception.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining("; "));
        
        Map<String, String> errorResponse = Map.of("error", errorMessage);
        
        log.errorf("Returning 400 BAD_REQUEST with error: %s", errorMessage);
        
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .build();
    }
}