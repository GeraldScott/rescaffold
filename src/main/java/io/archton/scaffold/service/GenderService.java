package io.archton.scaffold.service;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.repository.GenderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GenderService {

    private static final Logger log = Logger.getLogger(GenderService.class);

    @Inject
    GenderRepository genderRepository;

    public List<Gender> listAll() {
        return genderRepository.listAll();
    }

    public List<Gender> listSorted() {
        return genderRepository.listSorted();
    }

    public Gender findById(Long id) {
        return genderRepository.findById(id);
    }

    public Optional<Gender> findByIdOptional(Long id) {
        Gender gender = genderRepository.findById(id);
        return Optional.ofNullable(gender);
    }

    public Gender findByCode(String code) {
        return genderRepository.findByCode(code);
    }

    @Transactional
    public Gender createGender(Gender gender) {
        log.debugf("Creating gender with code: %s", gender.code);
        
        if (gender.id != null) {
            throw new IllegalArgumentException("ID must not be included in POST request");
        }
        
        validateGenderData(gender);
        normalizeGenderData(gender);
        checkDuplicateCode(gender.code);
        checkDuplicateDescription(gender.description);
        
        genderRepository.persist(gender);
        return gender;
    }

    @Transactional
    public Gender updateGender(Long id, Gender updates) {
        log.debugf("Updating gender id: %s", id);
        
        Gender existing = genderRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Entity not found with id: " + id);
        }

        if (updates.code != null && !updates.code.trim().isEmpty()) {
            normalizeCode(updates);
            if (updates.code.length() != 1) {
                throw new IllegalArgumentException("Gender code must be exactly 1 character");
            }
            if (!updates.code.matches("[A-Z]")) {
                throw new IllegalArgumentException("Gender code must be a single uppercase alphabetic character");
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
        genderRepository.persist(existing);
        return existing;
    }

    @Transactional
    public void deleteGender(Long id) {
        log.debugf("Deleting gender id: %s", id);
        
        Gender gender = genderRepository.findById(id);
        if (gender == null) {
            throw new IllegalArgumentException("Entity not found with id: " + id);
        }
        genderRepository.delete(gender);
    }

    private void validateGenderData(Gender gender) {
        if (gender.code == null || gender.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender code is required");
        }
        if (gender.code.length() != 1) {
            throw new IllegalArgumentException("Gender code must be exactly 1 character");
        }
        if (!gender.code.matches("[A-Z]")) {
            throw new IllegalArgumentException("Gender code must be a single uppercase alphabetic character");
        }
        if (gender.description == null || gender.description.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender description is required");
        }
    }

    private void normalizeGenderData(Gender gender) {
        normalizeCode(gender);
        normalizeDescription(gender);
    }

    private void normalizeCode(Gender gender) {
        if (gender.code != null) {
            gender.code = gender.code.trim().toUpperCase();
        }
    }

    private void normalizeDescription(Gender gender) {
        if (gender.description != null) {
            gender.description = gender.description.trim();
        }
    }

    private void checkDuplicateCode(String code) {
        if (genderRepository.findByCode(code) != null) {
            throw new IllegalArgumentException("Gender with code '" + code + "' already exists");
        }
    }

    private void checkDuplicateDescription(String description) {
        Gender existing = genderRepository.findByDescription(description);
        if (existing != null) {
            throw new IllegalArgumentException("Gender with description '" + description + "' already exists");
        }
    }

    private void checkDuplicateCodeForUpdate(String code, Long excludeId) {
        Gender existing = genderRepository.findByCodeExcludingId(code, excludeId);
        if (existing != null) {
            throw new IllegalArgumentException("Another gender with code '" + code + "' already exists");
        }
    }

    private void checkDuplicateDescriptionForUpdate(String description, Long excludeId) {
        Gender existing = genderRepository.findByDescriptionExcludingId(description, excludeId);
        if (existing != null) {
            throw new IllegalArgumentException("Another gender with description '" + description + "' already exists");
        }
    }
}