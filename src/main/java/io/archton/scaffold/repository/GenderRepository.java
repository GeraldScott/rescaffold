package io.archton.scaffold.repository;

import io.archton.scaffold.domain.Gender;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class GenderRepository implements PanacheRepository<Gender> {

    public Gender findByCode(String code) {
        return find("code", code).firstResult();
    }

    public List<Gender> listSorted() {
        return listAll(Sort.by("code"));
    }

    public Gender findByCodeExcludingId(String code, Long excludeId) {
        return find("code = ?1 and id != ?2", code, excludeId).firstResult();
    }

    public Gender findByDescriptionExcludingId(String description, Long excludeId) {
        return find("description = ?1 and id != ?2", description, excludeId).firstResult();
    }

    public Gender findByDescription(String description) {
        return find("description", description).firstResult();
    }
}