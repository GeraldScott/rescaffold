package io.archton.scaffold.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "gender")
public class Gender extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", length = 1, nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Code cannot be blank")
    @Size(min = 1, max = 1, message = "Code must be exactly 1 character")
    @Pattern(regexp = "[A-Z]", message = "Code must be a single uppercase alphabetic character")
    public String code;

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

    public Gender() {
        this.createdAt = LocalDateTime.now();
    }

    public Gender(String code, String description) {
        this();
        this.code = code;
        this.description = description;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Gender findByCode(String code) {
        return find("code", code).firstResult();
    }

    public static List<Gender> listSorted() {
        return listAll(Sort.by("code"));
    }
}
