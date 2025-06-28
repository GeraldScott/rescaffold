package io.archton.scaffold.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "person")
public class Person extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "first_name")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    public String firstName;

    @Column(name = "last_name", nullable = false)
    @NotNull
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    public String lastName;

    @Column(name = "email", unique = true)
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    public String email;

    @Column(name = "id_number")
    @Size(max = 50, message = "ID number must not exceed 50 characters")
    public String idNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_id")
    public IdType idType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id")
    public Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id")
    public Title title;

    @Column(name = "created_by", nullable = false)
    public String createdBy = "system";

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public Person() {
        this.createdAt = LocalDateTime.now();
    }

    public Person(String firstName, String lastName, String email) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Person findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static List<Person> listSorted() {
        return listAll(Sort.by("lastName", "firstName"));
    }

    public static List<Person> findByLastName(String lastName) {
        return find("lastName", lastName).list();
    }

    public static List<Person> findByGender(Gender gender) {
        return find("gender", gender).list();
    }

    public static List<Person> findByTitle(Title title) {
        return find("title", title).list();
    }

    public static List<Person> findByIdType(IdType idType) {
        return find("idType", idType).list();
    }

    public String getFullName() {
        if (firstName != null && !firstName.trim().isEmpty()) {
            return firstName + " " + lastName;
        }
        return lastName;
    }

    public String getDisplayName() {
        StringBuilder displayName = new StringBuilder();
        if (title != null) {
            displayName.append(title.description).append(" ");
        }
        displayName.append(getFullName());
        return displayName.toString();
    }
}