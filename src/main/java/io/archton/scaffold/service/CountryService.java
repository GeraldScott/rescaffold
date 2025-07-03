package io.archton.scaffold.service;

import io.archton.scaffold.domain.Country;
import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.repository.CountryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CountryService {

    private static final Logger log = Logger.getLogger(CountryService.class);

    @Inject
    CountryRepository countryRepository;

    public List<Country> listAll() {
        return countryRepository.listAll();
    }

    public List<Country> listSorted() {
        return countryRepository.listSorted();
    }

    public Country findById(Long id) {
        return countryRepository.findById(id);
    }

    public Optional<Country> findByIdOptional(Long id) {
        Country country = countryRepository.findById(id);
        return Optional.ofNullable(country);
    }

    public Country findByCode(String code) {
        return countryRepository.findByCode(code);
    }

    @Transactional
    public Country createCountry(Country country) {
        log.debugf("Creating country with code: %s", country.code);
        
        if (country.id != null) {
            throw new IllegalArgumentException("ID must not be included in POST request");
        }
        
        validateCountryData(country);
        normalizeCountryData(country);
        checkDuplicateCode(country.code);
        checkDuplicateName(country.name);
        
        countryRepository.persist(country);
        return country;
    }

    @Transactional
    public Country updateCountry(Long id, Country updates) {
        log.debugf("Updating country id: %s", id);
        
        Country existing = countryRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Entity not found with id: " + id);
        }

        if (updates.code != null && !updates.code.trim().isEmpty()) {
            normalizeCode(updates);
            if (updates.code.length() != 2) {
                throw new IllegalArgumentException("Country code must be exactly 2 characters");
            }
            if (!updates.code.matches("[A-Z]{2}")) {
                throw new IllegalArgumentException("Country code must be exactly 2 uppercase alphabetic characters");
            }
            checkDuplicateCodeForUpdate(updates.code, id);
            existing.code = updates.code;
        }
        
        if (updates.name != null && !updates.name.trim().isEmpty()) {
            normalizeName(updates);
            checkDuplicateNameForUpdate(updates.name, id);
            existing.name = updates.name;
        }

        if (updates.year != null) {
            existing.year = updates.year.trim().isEmpty() ? null : updates.year.trim();
        }

        if (updates.cctld != null) {
            existing.cctld = updates.cctld.trim().isEmpty() ? null : updates.cctld.trim();
        }
        
        existing.updatedAt = LocalDateTime.now();
        countryRepository.persist(existing);
        return existing;
    }

    @Transactional
    public void deleteCountry(Long id) {
        log.debugf("Deleting country id: %s", id);
        
        Country country = countryRepository.findById(id);
        if (country == null) {
            throw new IllegalArgumentException("Entity not found with id: " + id);
        }
        countryRepository.delete(country);
    }

    private void validateCountryData(Country country) {
        if (country.code == null || country.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Country code is required");
        }
        if (country.code.length() != 2) {
            throw new IllegalArgumentException("Country code must be exactly 2 characters");
        }
        if (!country.code.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("Country code must be exactly 2 uppercase alphabetic characters");
        }
        if (country.name == null || country.name.trim().isEmpty()) {
            throw new IllegalArgumentException("Country name is required");
        }
    }

    private void normalizeCountryData(Country country) {
        normalizeCode(country);
        normalizeName(country);
        if (country.year != null) {
            country.year = country.year.trim().isEmpty() ? null : country.year.trim();
        }
        if (country.cctld != null) {
            country.cctld = country.cctld.trim().isEmpty() ? null : country.cctld.trim();
        }
    }

    private void normalizeCode(Country country) {
        if (country.code != null) {
            country.code = country.code.trim().toUpperCase();
        }
    }

    private void normalizeName(Country country) {
        if (country.name != null) {
            country.name = country.name.trim();
        }
    }

    private void checkDuplicateCode(String code) {
        if (countryRepository.findByCode(code) != null) {
            throw new DuplicateEntityException("Country", "code", code);
        }
    }

    private void checkDuplicateName(String name) {
        Country existing = countryRepository.findByName(name);
        if (existing != null) {
            throw new DuplicateEntityException("Country", "name", name);
        }
    }

    private void checkDuplicateCodeForUpdate(String code, Long excludeId) {
        Country existing = countryRepository.findByCodeExcludingId(code, excludeId);
        if (existing != null) {
            throw new DuplicateEntityException("Country", "code", code, "update");
        }
    }

    private void checkDuplicateNameForUpdate(String name, Long excludeId) {
        Country existing = countryRepository.findByNameExcludingId(name, excludeId);
        if (existing != null) {
            throw new DuplicateEntityException("Country", "name", name, "update");
        }
    }
}