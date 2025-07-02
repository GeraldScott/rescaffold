package io.archton.scaffold.repository;

import io.archton.scaffold.domain.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public User findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }
}