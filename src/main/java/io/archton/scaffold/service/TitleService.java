package io.archton.scaffold.service;

import io.archton.scaffold.domain.Title;
import io.archton.scaffold.repository.TitleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TitleService {

    private static final Logger log = Logger.getLogger(TitleService.class);

    @Inject
    TitleRepository titleRepository;

    public List<Title> listAll() {
        return titleRepository.listAll();
    }

    public List<Title> listSorted() {
        return titleRepository.listSorted();
    }

    public Title findById(Long id) {
        return titleRepository.findById(id);
    }

    public Optional<Title> findByIdOptional(Long id) {
        Title title = titleRepository.findById(id);
        return Optional.ofNullable(title);
    }

    public Title findByCode(String code) {
        return titleRepository.findByCode(code);
    }

    @Transactional
    public Title createTitle(Title title) {
        log.debugf("Creating title with code: %s", title.code);
        
        if (title.id != null) {
            throw new IllegalArgumentException("ID must not be included in POST request");
        }
        
        validateTitleData(title);
        normalizeTitleData(title);
        checkDuplicateCode(title.code);
        checkDuplicateDescription(title.description);
        
        titleRepository.persist(title);
        return title;
    }

    @Transactional
    public Title updateTitle(Long id, Title updates) {
        log.debugf("Updating title id: %s", id);
        
        Title existing = titleRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Entity not found with id: " + id);
        }

        if (updates.code != null && !updates.code.trim().isEmpty()) {
            normalizeCode(updates);
            if (updates.code.length() < 1 || updates.code.length() > 5) {
                throw new IllegalArgumentException("Title code must be between 1 and 5 characters");
            }
            if (!updates.code.matches("[A-Z]+")) {
                throw new IllegalArgumentException("Title code must contain only uppercase letters");
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
        titleRepository.persist(existing);
        return existing;
    }

    @Transactional
    public void deleteTitle(Long id) {
        log.debugf("Deleting title id: %s", id);
        
        Title title = titleRepository.findById(id);
        if (title == null) {
            throw new IllegalArgumentException("Entity not found with id: " + id);
        }
        titleRepository.delete(title);
    }

    private void validateTitleData(Title title) {
        if (title.code == null || title.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Title code is required");
        }
        if (title.code.length() < 1 || title.code.length() > 5) {
            throw new IllegalArgumentException("Title code must be between 1 and 5 characters");
        }
        if (!title.code.matches("[A-Z]+")) {
            throw new IllegalArgumentException("Title code must contain only uppercase letters");
        }
        if (title.description == null || title.description.trim().isEmpty()) {
            throw new IllegalArgumentException("Title description is required");
        }
    }

    private void normalizeTitleData(Title title) {
        normalizeCode(title);
        normalizeDescription(title);
    }

    private void normalizeCode(Title title) {
        if (title.code != null) {
            title.code = title.code.trim().toUpperCase();
        }
    }

    private void normalizeDescription(Title title) {
        if (title.description != null) {
            title.description = title.description.trim();
        }
    }

    private void checkDuplicateCode(String code) {
        if (titleRepository.findByCode(code) != null) {
            throw new IllegalArgumentException("Title with code '" + code + "' already exists");
        }
    }

    private void checkDuplicateDescription(String description) {
        Title existing = titleRepository.findByDescription(description);
        if (existing != null) {
            throw new IllegalArgumentException("Title with description '" + description + "' already exists");
        }
    }

    private void checkDuplicateCodeForUpdate(String code, Long excludeId) {
        Title existing = titleRepository.findByCodeExcludingId(code, excludeId);
        if (existing != null) {
            throw new IllegalArgumentException("Another title with code '" + code + "' already exists");
        }
    }

    private void checkDuplicateDescriptionForUpdate(String description, Long excludeId) {
        Title existing = titleRepository.findByDescriptionExcludingId(description, excludeId);
        if (existing != null) {
            throw new IllegalArgumentException("Another title with description '" + description + "' already exists");
        }
    }
}