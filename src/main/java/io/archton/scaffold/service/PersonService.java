package io.archton.scaffold.service;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.repository.PersonRepository;
import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PersonService {

    private static final Logger log = Logger.getLogger(PersonService.class);

    @Inject
    PersonRepository personRepository;

    public List<Person> listAll() {
        return personRepository.listAll();
    }

    public List<Person> listSorted() {
        return personRepository.listSorted();
    }

    public Person findById(Long id) {
        return personRepository.findById(id);
    }

    public Optional<Person> findByIdOptional(Long id) {
        Person person = personRepository.findById(id);
        return Optional.ofNullable(person);
    }

    public Person findByEmail(String email) {
        return personRepository.findByEmail(email);
    }

    @Transactional
    public Person createPerson(Person person) {
        log.debugf("Creating person with email: %s", person.email);

        if (person.id != null) {
            throw new ValidationException("id", "ID must not be included in POST request");
        }

        normalizePersonData(person);
        checkDuplicateEmail(person.email);

        personRepository.persist(person);
        return person;
    }

    @Transactional
    public Person updatePerson(Long id, Person updates) {
        log.debugf("Updating person id: %s", id);

        Person existing = personRepository.findById(id);
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


        // Update relationships
        existing.title = updates.title;
        existing.gender = updates.gender;


        existing.updatedAt = LocalDateTime.now();
        personRepository.persist(existing);
        return existing;
    }

    @Transactional
    public void deletePerson(Long id) {
        log.debugf("Deleting person id: %s", id);

        Person person = personRepository.findById(id);
        if (person == null) {
            throw new EntityNotFoundException("Person", id);
        }

        personRepository.delete(person);
    }


    private void normalizePersonData(Person person) {
        normalizeFirstName(person);
        normalizeLastName(person);
        normalizeEmail(person);
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


    private void checkDuplicateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (personRepository.findByEmail(email) != null) {
                throw new DuplicateEntityException("Person", "email", email);
            }
        }
    }

    private void checkDuplicateEmailForUpdate(String email, Long excludeId) {
        if (email != null && !email.trim().isEmpty()) {
            Person existing = personRepository.findByEmailExcludingId(email, excludeId);
            if (existing != null) {
                throw new DuplicateEntityException("Person", "email", email, "update");
            }
        }
    }




}