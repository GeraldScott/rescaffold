package io.archton.scaffold.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "gender")
public class Gender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", length = 1, nullable = false, unique = true)
    @NotNull(message = "Gender code is required")
    @NotBlank(message = "Gender code is required")
    @Size(min = 1, max = 1, message = "Gender code must be exactly 1 character")
    @Pattern(regexp = "[A-Z]", message = "Gender code must be a single uppercase alphabetic character")
    public String code;

    @Column(name = "description", columnDefinition = "text", nullable = false, unique = true)
    @NotNull(message = "Gender description is required")
    @NotBlank(message = "Gender description is required")
    public String description;

    @Column(name = "created_by", nullable = false)
    public String createdBy = "system";

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public Gender() {
        this.createdAt = LocalDateTime.now();
    }

    public Gender(String code, String description) {
        this();
        this.code = code;
        this.description = description;
    }

}
