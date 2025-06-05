package io.archton.scaffold.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "title")
public class Title extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", length = 5, nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Code cannot be blank")
    @Size(max = 5, message = "Code cannot exceed 5 characters")
    public String code;

    @Column(name = "description", nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Description cannot be blank")
    public String description;

    public Title() {
    }

    public Title(String code, String description) {
        this.code = code;
        this.description = description;
    }
}