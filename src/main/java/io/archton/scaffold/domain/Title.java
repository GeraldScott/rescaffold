package io.archton.scaffold.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "title")
public class Title extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Code cannot be blank")
    public String code;

    @Column(name = "description", columnDefinition = "text", nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Description cannot be blank")
    public String description;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "created_by", nullable = false)
    public String createdBy;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public Title() {
        this.createdAt = LocalDateTime.now();
    }

    public Title(String code, String description) {
        this();
        this.code = code;
        this.description = description;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}