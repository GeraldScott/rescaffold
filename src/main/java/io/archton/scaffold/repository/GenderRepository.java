package io.archton.scaffold.repository;

import io.archton.scaffold.domain.Gender;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GenderRepository implements PanacheRepositoryBase<Gender, UUID> {

    public Gender findByCode(String code) {
        return find("code", code).firstResult();
    }

    public List<Gender> listSorted() {
        return listAll(Sort.by("code"));
    }

    @Transactional
    public Gender createGender(Gender gender) {
        if (gender.id != null) {
            throw new IllegalArgumentException("ID must not be included when creating a new gender");
        }
        
        validateGenderData(gender);
        checkDuplicateCode(gender.code);
        checkDuplicateDescription(gender.description);
        
        gender.persist();
        return gender;
    }

    @Transactional
    public Gender updateGender(UUID id, Gender updates) {
        Gender existing = findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Gender not found with id: " + id);
        }

        if (updates.code != null) {
            checkDuplicateCodeForUpdate(updates.code, id);
            existing.code = updates.code;
        }
        
        if (updates.description != null) {
            checkDuplicateDescriptionForUpdate(updates.description, id);
            existing.description = updates.description;
        }

        existing.persist();
        return existing;
    }

    @Transactional
    public void deleteGender(UUID id) {
        Gender gender = findById(id);
        if (gender == null) {
            throw new IllegalArgumentException("Gender not found with id: " + id);
        }
        gender.delete();
    }

    public Optional<Gender> findByIdOptional(UUID id) {
        Gender gender = findById(id);
        return Optional.ofNullable(gender);
    }

    private void validateGenderData(Gender gender) {
        if (gender.code == null || gender.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender code is required");
        }
        if (gender.description == null || gender.description.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender description is required");
        }
    }

    private void checkDuplicateCode(String code) {
        if (findByCode(code) != null) {
            throw new IllegalArgumentException("Gender with code '" + code + "' already exists");
        }
    }

    private void checkDuplicateDescription(String description) {
        Gender existing = find("description", description).firstResult();
        if (existing != null) {
            throw new IllegalArgumentException("Gender with description '" + description + "' already exists");
        }
    }

    private void checkDuplicateCodeForUpdate(String code, UUID excludeId) {
        Gender existing = find("code = ?1 and id != ?2", code, excludeId).firstResult();
        if (existing != null) {
            throw new IllegalArgumentException("Another gender with code '" + code + "' already exists");
        }
    }

    private void checkDuplicateDescriptionForUpdate(String description, UUID excludeId) {
        Gender existing = find("description = ?1 and id != ?2", description, excludeId).firstResult();
        if (existing != null) {
            throw new IllegalArgumentException("Another gender with description '" + description + "' already exists");
        }
    }
}