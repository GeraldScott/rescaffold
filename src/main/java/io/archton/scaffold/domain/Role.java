package io.archton.scaffold.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "name", nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Role name cannot be blank")
    @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role name must contain only uppercase letters and underscores")
    public String name;

    @Column(name = "description", columnDefinition = "text", nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Description cannot be blank")
    public String description;

    @Column(name = "created_by", nullable = false)
    public String createdBy = "system";

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "roles")
    public Set<User> users = new HashSet<>();

    public Role() {
        this.createdAt = LocalDateTime.now();
    }

    public Role(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
}