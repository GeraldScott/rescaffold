package io.archton.scaffold.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
@Table(name = "id_type")
public class IdType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", nullable = false, unique = true, length = 5)
    @NotNull(message = "IdType code is required")
    @NotBlank(message = "IdType code is required")
    @Size(min = 1, max = 5, message = "IdType code must be between 1 and 5 characters")
    @Pattern(regexp = "[A-Z]+", message = "IdType code must contain only uppercase letters")
    public String code;

    @Column(name = "description", columnDefinition = "text", nullable = false, unique = true)
    @NotNull(message = "IdType description is required")
    @NotBlank(message = "IdType description is required")
    public String description;

    @Column(name = "created_by", nullable = false)
    public String createdBy = "system";

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public IdType() {
        this.createdAt = LocalDateTime.now();
    }

    public IdType(String code, String description) {
        this();
        this.code = code;
        this.description = description;
    }

}