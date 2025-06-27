package io.archton.scaffold.service;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PersonService {

    private static final Logger log = Logger.getLogger(PersonService.class);

    public List<Person> listAll() {
        return Person.listAll();
    }

    public List<Person> listSorted() {
        return Person.list("isActive = true order by lastName, firstName");
    }

    public Person findById(Long id) {
        return Person.findById(id);
    }

    public Optional<Person> findByIdOptional(Long id) {
        Person person = Person.findById(id);
        return Optional.ofNullable(person);
    }

    public Person findByEmail(String email) {
        return Person.findByEmail(email);
    }

    @Transactional
    public Person createPerson(Person person) {
        log.debugf("Creating person with email: %s", person.email);
        
        if (person.id != null) {
            throw new ValidationException("id", "ID must not be included in POST request");
        }
        
        validatePersonData(person);
        normalizePersonData(person);
        checkDuplicateEmail(person.email);
        
        person.persist();
        return person;
    }

    @Transactional
    public Person updatePerson(Long id, Person updates) {
        log.debugf("Updating person id: %s", id);
        
        Person existing = Person.findById(id);
        if (existing == null) {
            throw new EntityNotFoundException("Person", id);
        }

        if (updates.firstName != null) {
            normalizeFirstName(updates);
            existing.firstName = updates.firstName;
        }
        
        if (updates.lastName != null && !updates.lastName.trim().isEmpty()) {
            normalizeLastName(updates);
            existing.lastName = updates.lastName;
        }
        
        if (updates.email != null) {
            normalizeEmail(updates);
            checkDuplicateEmailForUpdate(updates.email, id);
            existing.email = updates.email;
        }
        
        if (updates.idNumber != null) {
            normalizeIdNumber(updates);
            existing.idNumber = updates.idNumber;
        }
        
        // Update relationships
        existing.title = updates.title;
        existing.gender = updates.gender;
        existing.idType = updates.idType;
        
        // Update isActive field
        existing.isActive = updates.isActive;

        existing.persist();
        return existing;
    }

    @Transactional
    public void deletePerson(Long id) {
        log.debugf("Soft deleting person id: %s", id);
        
        Person person = Person.findById(id);
        if (person == null) {
            throw new EntityNotFoundException("Person", id);
        }
        
        // Soft delete by setting isActive to false
        person.isActive = false;
        person.persist();
    }

    private void validatePersonData(Person person) {
        if (person.lastName == null || person.lastName.trim().isEmpty()) {
            throw new ValidationException("lastName", "Last name is required");
        }
        
        if (person.email != null && !person.email.trim().isEmpty()) {
            if (!isValidEmail(person.email)) {
                throw new ValidationException("email", "Invalid email format");
            }
        }
    }

    private void normalizePersonData(Person person) {
        normalizeFirstName(person);
        normalizeLastName(person);
        normalizeEmail(person);
        normalizeIdNumber(person);
    }

    private void normalizeFirstName(Person person) {
        if (person.firstName != null) {
            person.firstName = person.firstName.trim();
            if (person.firstName.isEmpty()) {
                person.firstName = null;
            }
        }
    }

    private void normalizeLastName(Person person) {
        if (person.lastName != null) {
            person.lastName = person.lastName.trim();
        }
    }

    private void normalizeEmail(Person person) {
        if (person.email != null) {
            person.email = person.email.trim().toLowerCase();
            if (person.email.isEmpty()) {
                person.email = null;
            }
        }
    }

    private void normalizeIdNumber(Person person) {
        if (person.idNumber != null) {
            person.idNumber = person.idNumber.trim();
            if (person.idNumber.isEmpty()) {
                person.idNumber = null;
            }
        }
    }

    private void checkDuplicateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (Person.findByEmail(email) != null) {
                throw new DuplicateEntityException("Person", "email", email);
            }
        }
    }

    private void checkDuplicateEmailForUpdate(String email, Long excludeId) {
        if (email != null && !email.trim().isEmpty()) {
            Person existing = Person.find("email = ?1 and id != ?2", email, excludeId).firstResult();
            if (existing != null) {
                throw new DuplicateEntityException("Person", "email", email, "update");
            }
        }
    }

    private boolean isValidEmail(String email) {
        // Basic email validation - more comprehensive validation is handled by Jakarta Bean Validation
        return email != null && email.contains("@") && email.contains(".");
    }
}