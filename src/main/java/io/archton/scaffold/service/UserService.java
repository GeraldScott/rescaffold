package io.archton.scaffold.service;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.domain.Role;
import io.archton.scaffold.domain.User;
import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.exception.ValidationException;
import io.archton.scaffold.repository.PersonRepository;
import io.archton.scaffold.repository.RoleRepository;
import io.archton.scaffold.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    PersonRepository personRepository;

    public List<User> listAll() {
        return userRepository.listAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByIdOptional(Long id) {
        User user = userRepository.findById(id);
        return Optional.ofNullable(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User createUser(User user, String password, Long personId, List<String> roleNames) {
        log.debugf("Creating user with username: %s", user.username);

        if (user.id != null) {
            throw new ValidationException("id", "ID must not be included in POST request");
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(user.username)) {
            throw new DuplicateEntityException("User", "username", user.username);
        }

        // Hash the password
        user.passwordHash = BcryptUtil.bcryptHash(password);

        // Link to Person if provided
        if (personId != null) {
            Person person = personRepository.findById(personId);
            if (person == null) {
                throw new EntityNotFoundException("Person", personId);
            }
            user.person = person;
        }

        // Assign roles
        if (roleNames != null && !roleNames.isEmpty()) {
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName);
                if (role == null) {
                    throw new EntityNotFoundException("Role", roleName);
                }
                user.addRole(role);
            }
        } else {
            // Assign default ROLE_USER if no roles specified
            Role defaultRole = roleRepository.findByName("ROLE_USER");
            if (defaultRole != null) {
                user.addRole(defaultRole);
            }
        }

        userRepository.persist(user);
        return user;
    }

    @Transactional
    public User updateUser(Long id, User updates, String newPassword) {
        log.debugf("Updating user id: %s", id);

        User existing = userRepository.findById(id);
        if (existing == null) {
            throw new EntityNotFoundException("User", id);
        }

        // Update username if provided and different
        if (updates.username != null && !updates.username.equals(existing.username)) {
            // Validate username uniqueness
            if (userRepository.existsByUsername(updates.username)) {
                throw new DuplicateEntityException("User", "username", updates.username, "update");
            }
            existing.username = updates.username;
        }

        // Update password if provided
        if (newPassword != null && !newPassword.isEmpty()) {
            existing.passwordHash = BcryptUtil.bcryptHash(newPassword);
        }

        // Update person if provided
        if (updates.person != null && (existing.person == null || !updates.person.id.equals(existing.person.id))) {
            existing.person = updates.person;
        }

        existing.updatedAt = LocalDateTime.now();
        userRepository.persist(existing);
        return existing;
    }

    @Transactional
    public void deleteUser(Long id) {
        log.debugf("Deleting user id: %s", id);

        User user = userRepository.findById(id);
        if (user == null) {
            throw new EntityNotFoundException("User", id);
        }
        userRepository.delete(user);
    }

    @Transactional
    public User updateLastLogin(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new EntityNotFoundException("User", id);
        }
        user.lastLogin = LocalDateTime.now();
        userRepository.persist(user);
        return user;
    }

    @Transactional
    public User addRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User", userId);
        }

        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            throw new EntityNotFoundException("Role", roleName);
        }

        user.addRole(role);
        userRepository.persist(user);
        return user;
    }

    @Transactional
    public User removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User", userId);
        }

        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            throw new EntityNotFoundException("Role", roleName);
        }

        user.removeRole(role);
        userRepository.persist(user);
        return user;
    }

    public boolean verifyPassword(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        return BcryptUtil.matches(password, user.passwordHash);
    }
}