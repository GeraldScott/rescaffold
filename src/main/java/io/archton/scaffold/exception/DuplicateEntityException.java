package io.archton.scaffold.exception;

/**
 * Exception thrown when attempting to create or update an entity with duplicate unique values.
 */
public class DuplicateEntityException extends RuntimeException {
    
    private final String entityType;
    private final String fieldName;
    private final Object fieldValue;
    private final String operation;
    
    public DuplicateEntityException(String entityType, String fieldName, Object fieldValue) {
        this(entityType, fieldName, fieldValue, "create");
    }
    
    public DuplicateEntityException(String entityType, String fieldName, Object fieldValue, String operation) {
        super(String.format("%s with %s '%s' already exists", entityType, fieldName, fieldValue));
        this.entityType = entityType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.operation = operation;
    }
    
    public DuplicateEntityException(String entityType, String fieldName, Object fieldValue, Throwable cause) {
        this(entityType, fieldName, fieldValue, "create", cause);
    }
    
    public DuplicateEntityException(String entityType, String fieldName, Object fieldValue, String operation, Throwable cause) {
        super(String.format("%s with %s '%s' already exists", entityType, fieldName, fieldValue), cause);
        this.entityType = entityType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.operation = operation;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
    
    public String getOperation() {
        return operation;
    }
}