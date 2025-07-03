package io.archton.scaffold.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", length = 2, nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Code cannot be blank")
    @Size(min = 2, max = 2, message = "Code must be exactly 2 characters")
    @Pattern(regexp = "[A-Z]{2}", message = "Code must be exactly 2 uppercase alphabetic characters")
    public String code;

    @Column(name = "name", columnDefinition = "text", nullable = false, unique = true)
    @NotNull
    @NotBlank(message = "Name cannot be blank")
    public String name;

    @Column(name = "year", columnDefinition = "text")
    public String year;

    @Column(name = "cctld", columnDefinition = "text")
    public String cctld;

    @Column(name = "created_by", nullable = false)
    public String createdBy = "system";

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public Country() {
        this.createdAt = LocalDateTime.now();
    }

    public Country(String code, String name) {
        this();
        this.code = code;
        this.name = name;
    }

    public Country(String code, String name, String year, String cctld) {
        this();
        this.code = code;
        this.name = name;
        this.year = year;
        this.cctld = cctld;
    }

}