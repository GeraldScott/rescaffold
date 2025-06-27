package io.archton.scaffold.exception;

/**
 * Exception thrown when an entity is not found in the database.
 */
public class EntityNotFoundException extends RuntimeException {
    
    private final String entityType;
    private final Object id;
    
    public EntityNotFoundException(String entityType, Object id) {
        super(String.format("%s not found with id: %s", entityType, id));
        this.entityType = entityType;
        this.id = id;
    }
    
    public EntityNotFoundException(String entityType, Object id, Throwable cause) {
        super(String.format("%s not found with id: %s", entityType, id), cause);
        this.entityType = entityType;
        this.id = id;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public Object getId() {
        return id;
    }
}