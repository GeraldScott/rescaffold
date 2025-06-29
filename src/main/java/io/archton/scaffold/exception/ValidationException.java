package io.archton.scaffold.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when entity validation fails.
 */
public class ValidationException extends RuntimeException {
    
    private final Map<String, String> errors;
    
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }
    
    public ValidationException(String fieldName, String errorMessage) {
        super(errorMessage);
        this.errors = new HashMap<>();
        this.errors.put(fieldName, errorMessage);
    }
    
    public ValidationException(Map<String, String> errors) {
        super("Validation failed with " + errors.size() + " error(s)");
        this.errors = new HashMap<>(errors);
    }
    
    public Map<String, String> getErrors() {
        return new HashMap<>(errors);
    }
    
    public void addError(String fieldName, String errorMessage) {
        errors.put(fieldName, errorMessage);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}