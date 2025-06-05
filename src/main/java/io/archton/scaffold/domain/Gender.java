package io.archton.scaffold.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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

    public Gender() {
    }

    public Gender(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
