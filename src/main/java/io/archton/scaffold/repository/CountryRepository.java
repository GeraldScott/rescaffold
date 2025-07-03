package io.archton.scaffold.repository;

import io.archton.scaffold.domain.Country;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CountryRepository implements PanacheRepository<Country> {

    public Country findByCode(String code) {
        return find("code", code).firstResult();
    }

    public List<Country> listSorted() {
        return listAll(Sort.by("name"));
    }

    public Country findByCodeExcludingId(String code, Long excludeId) {
        return find("code = ?1 and id != ?2", code, excludeId).firstResult();
    }

    public Country findByNameExcludingId(String name, Long excludeId) {
        return find("name = ?1 and id != ?2", name, excludeId).firstResult();
    }

    public Country findByName(String name) {
        return find("name", name).firstResult();
    }
}