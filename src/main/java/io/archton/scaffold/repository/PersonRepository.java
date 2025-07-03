package io.archton.scaffold.repository;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.domain.Title;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PersonRepository implements PanacheRepository<Person> {

    public Person findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public List<Person> listSorted() {
        return listAll(Sort.by("lastName", "firstName"));
    }

    public List<Person> findByLastName(String lastName) {
        return find("lastName", lastName).list();
    }

    public List<Person> findByGender(Gender gender) {
        return find("gender", gender).list();
    }

    public List<Person> findByTitle(Title title) {
        return find("title", title).list();
    }


    public Person findByEmailExcludingId(String email, Long excludeId) {
        return find("email = ?1 and id != ?2", email, excludeId).firstResult();
    }



}