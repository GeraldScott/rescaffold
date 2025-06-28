package io.archton.scaffold.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;
import io.quarkus.panache.common.Sort;

@Entity
@Table(name = "id_type")
public class IdType extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", nullable = false, unique = true, length = 5)
    @NotNull
    @NotBlank(message = "Code cannot be blank")
    @Size(min = 1, max = 5, message = "Code must be between 1 and 5 characters")
    @Pattern(regexp = "[A-Z]+", message = "Code must contain only uppercase letters")
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

    public IdType() {
        this.createdAt = LocalDateTime.now();
    }

    public IdType(String code, String description) {
        this();
        this.code = code;
        this.description = description;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static IdType findByCode(String code) {
        return find("code", code).firstResult();
    }

    public static List<IdType> listSorted() {
        return listAll(Sort.ascending("description"));
    }
}