package io.archton.scaffold.web;

import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.exception.ValidationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * Base class for entity web routers with common exception handling methods.
 * This reduces code duplication across entity routers and standardizes error handling.
 * 
 * @param <T> The entity type
 */
public abstract class BaseEntityRouter<T> {
    
    private static final Logger log = Logger.getLogger(BaseEntityRouter.class);
    
    /**
     * Handles creation/update exceptions for entity forms.
     * Preserves user input data by re-rendering the form with the populated entity.
     * 
     * @param e The exception that occurred
     * @param entity The entity populated with user input data
     * @param errorContext Description of the operation for logging
     * @param fragmentName The template fragment to render
     * @return Response with re-rendered form containing user data and error message
     */
    protected Response handleEntityFormException(Exception e, T entity, String errorContext, String fragmentName) {
        if (e instanceof ValidationException) {
            // Handle validation errors (preserve form data)
            log.debug("Validation error " + errorContext + ": " + e.getMessage());
            String errorMessage = e.getMessage();
            
            // Re-render the form with error message and user's input data
            return Response.ok(
                renderFragment(fragmentName, entity, errorMessage)
            ).build();
            
        } else if (e instanceof DuplicateEntityException) {
            // Handle duplicate entity exceptions (preserve form data)
            log.debug("Duplicate entity " + errorContext + ": " + e.getMessage());
            String errorMessage = e.getMessage();
            
            // Re-render the form with error message and user's input data
            return Response.ok(
                renderFragment(fragmentName, entity, errorMessage)
            ).build();
            
        } else if (e instanceof EntityNotFoundException) {
            // Handle entity not found exceptions
            log.debug("Entity not found " + errorContext + ": " + e.getMessage());
            String errorMessage = e.getMessage();
            
            // Usually redirect to table view with error message
            return Response.ok(
                renderTableWithError(errorMessage)
            ).build();
            
        } else {
            // Handle unexpected errors
            log.error("Unexpected error " + errorContext + ": " + e.getMessage(), e);
            String errorMessage = "An unexpected error occurred. Please try again.";
            
            // Re-render the form with error message and user's input data
            return Response.ok(
                renderFragment(fragmentName, entity, errorMessage)
            ).build();
        }
    }
    
    /**
     * Handles deletion exceptions for entity delete operations.
     * 
     * @param e The exception that occurred
     * @param errorContext Description of the operation for logging
     * @return Response with table view and error message
     */
    protected Response handleEntityDeleteException(Exception e, String errorContext) {
        log.error("Error " + errorContext + ": " + e.getMessage(), e);
        String errorMessage;
        
        if (e instanceof EntityNotFoundException) {
            errorMessage = e.getMessage();
        } else {
            errorMessage = "An unexpected error occurred during deletion. Please try again.";
        }
        
        // Redirect to table with error message
        return Response.ok(
            renderTableWithError(errorMessage)
        ).build();
    }
    
    /**
     * Helper method to execute an operation and handle success/error responses.
     * 
     * @param operation The operation to execute
     * @param entity The entity with user input data
     * @param errorContext Description for logging
     * @param fragmentName Fragment to render on error
     * @param successResponse Response to return on success
     * @return Response with either success or error handling
     */
    protected Response executeWithErrorHandling(Runnable operation, T entity, String errorContext, String fragmentName, Response successResponse) {
        try {
            operation.run();
            return successResponse;
        } catch (Exception e) {
            return handleEntityFormException(e, entity, errorContext, fragmentName);
        }
    }
    
    /**
     * Abstract method to render a specific fragment of the template.
     * Each router must implement this based on their specific template signature.
     * 
     * @param fragmentName The name of the fragment to render
     * @param entity The entity to pass to the template
     * @param errorMessage The error message to display
     * @return The rendered HTML
     */
    protected abstract String renderFragment(String fragmentName, T entity, String errorMessage);
    
    /**
     * Abstract method to render the table view with an error message.
     * Each router must implement this based on their specific template signature.
     * 
     * @param errorMessage The error message to display
     * @return The rendered HTML
     */
    protected abstract String renderTableWithError(String errorMessage);
}