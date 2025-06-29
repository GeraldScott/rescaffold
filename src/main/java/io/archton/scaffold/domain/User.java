package io.archton.scaffold.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_login") //
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "username", nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    public String username;

    @Column(name = "password_hash", nullable = false)
    @NotNull
    @NotBlank(message = "Password hash cannot be blank")
    public String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id")
    public Person person;

    @Column(name = "last_login")
    public LocalDateTime lastLogin;

    @Column(name = "created_by", nullable = false)
    public String createdBy = "system";

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    public Set<Role> roles = new HashSet<>();

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.name.equals(roleName));
    }
}