package io.archton.scaffold.service;

import io.archton.scaffold.domain.IdType;
import io.archton.scaffold.repository.IdTypeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class IdTypeService {

    private static final Logger log = Logger.getLogger(IdTypeService.class);

    @Inject
    IdTypeRepository idTypeRepository;

    public List<IdType> listAll() {
        return idTypeRepository.listAll();
    }

    public List<IdType> listSorted() {
        return idTypeRepository.listSorted();
    }

    public IdType findById(Long id) {
        return idTypeRepository.findById(id);
    }

    public Optional<IdType> findByIdOptional(Long id) {
        IdType idType = idTypeRepository.findById(id);
        return Optional.ofNullable(idType);
    }

    public IdType findByCode(String code) {
        return idTypeRepository.findByCode(code);
    }

    @Transactional
    public IdType createIdType(IdType idType) {
        log.debugf("Creating id type with code: %s", idType.code);
        
        if (idType.id != null) {
            throw new IllegalArgumentException("ID must not be included in POST request");
        }
        
        validateIdTypeData(idType);
        normalizeIdTypeData(idType);
        checkDuplicateCode(idType.code);
        checkDuplicateDescription(idType.description);
        
        idTypeRepository.persist(idType);
        return idType;
    }

    @Transactional
    public IdType updateIdType(Long id, IdType updates) {
        log.debugf("Updating id type id: %s", id);
        
        IdType existing = idTypeRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Entity not found with id: " + id);
        }

        if (updates.code != null && !updates.code.trim().isEmpty()) {
            normalizeCode(updates);
            if (updates.code.length() < 1 || updates.code.length() > 5) {
                throw new IllegalArgumentException("ID type code must be between 1 and 5 characters");
            }
            if (!updates.code.matches("[A-Z]+")) {
                throw new IllegalArgumentException("ID type code must contain only uppercase letters");
            }
            checkDuplicateCodeForUpdate(updates.code, id);
            existing.code = updates.code;
        }
        
        if (updates.description != null && !updates.description.trim().isEmpty()) {
            normalizeDescription(updates);
            checkDuplicateDescriptionForUpdate(updates.description, id);
            existing.description = updates.description;
        }
        
        existing.updatedAt = LocalDateTime.now();
        idTypeRepository.persist(existing);
        return existing;
    }

    @Transactional
    public void deleteIdType(Long id) {
        log.debugf("Deleting id type id: %s", id);
        
        IdType idType = idTypeRepository.findById(id);
        if (idType == null) {
            throw new IllegalArgumentException("Entity not found with id: " + id);
        }
        idTypeRepository.delete(idType);
    }

    private void validateIdTypeData(IdType idType) {
        if (idType.code == null || idType.code.trim().isEmpty()) {
            throw new IllegalArgumentException("ID type code is required");
        }
        if (idType.code.length() < 1 || idType.code.length() > 5) {
            throw new IllegalArgumentException("ID type code must be between 1 and 5 characters");
        }
        if (!idType.code.matches("[A-Z]+")) {
            throw new IllegalArgumentException("ID type code must contain only uppercase letters");
        }
        if (idType.description == null || idType.description.trim().isEmpty()) {
            throw new IllegalArgumentException("ID type description is required");
        }
    }

    private void normalizeIdTypeData(IdType idType) {
        normalizeCode(idType);
        normalizeDescription(idType);
    }

    private void normalizeCode(IdType idType) {
        if (idType.code != null) {
            idType.code = idType.code.trim().toUpperCase();
        }
    }

    private void normalizeDescription(IdType idType) {
        if (idType.description != null) {
            idType.description = idType.description.trim();
        }
    }

    private void checkDuplicateCode(String code) {
        if (idTypeRepository.findByCode(code) != null) {
            throw new IllegalArgumentException("ID type with code '" + code + "' already exists");
        }
    }

    private void checkDuplicateDescription(String description) {
        IdType existing = idTypeRepository.findByDescription(description);
        if (existing != null) {
            throw new IllegalArgumentException("ID type with description '" + description + "' already exists");
        }
    }

    private void checkDuplicateCodeForUpdate(String code, Long excludeId) {
        IdType existing = idTypeRepository.findByCodeExcludingId(code, excludeId);
        if (existing != null) {
            throw new IllegalArgumentException("Another ID type with code '" + code + "' already exists");
        }
    }

    private void checkDuplicateDescriptionForUpdate(String description, Long excludeId) {
        IdType existing = idTypeRepository.findByDescriptionExcludingId(description, excludeId);
        if (existing != null) {
            throw new IllegalArgumentException("Another ID type with description '" + description + "' already exists");
        }
    }
}