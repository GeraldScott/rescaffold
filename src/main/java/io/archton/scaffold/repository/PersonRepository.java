package io.archton.scaffold.repository;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.domain.Title;
import io.archton.scaffold.domain.IdType;
import io.archton.scaffold.domain.Country;
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

    public List<Person> findByIdType(IdType idType) {
        return find("idType", idType).list();
    }

    public Person findByEmailExcludingId(String email, Long excludeId) {
        return find("email = ?1 and id != ?2", email, excludeId).firstResult();
    }

    public List<Person> findByCountry(Country country) {
        return find("country", country).list();
    }

    public Person findByCountryAndIdNumber(Country country, String idNumber) {
        if (country == null || idNumber == null) {
            return null;
        }
        return find("country = ?1 and idNumber = ?2", country, idNumber).firstResult();
    }

    public Person findByCountryAndIdNumberExcludingId(Country country, String idNumber, Long excludeId) {
        if (country == null || idNumber == null) {
            return null;
        }
        return find("country = ?1 and idNumber = ?2 and id != ?3", country, idNumber, excludeId).firstResult();
    }
}