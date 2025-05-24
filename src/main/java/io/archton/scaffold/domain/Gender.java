package io.archton.scaffold.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "gender")
public class Gender extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
        
    @Column(name = "code", length = 1, nullable = false, unique = true)
    @NotNull
    public String code;

    @Column(name = "description", nullable = false, unique = true)
    @NotNull
    public String description;

    public Gender() {
    }

    public Gender(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
