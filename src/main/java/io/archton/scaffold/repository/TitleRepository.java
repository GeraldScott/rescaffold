package io.archton.scaffold.repository;

import io.archton.scaffold.domain.Title;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TitleRepository implements PanacheRepository<Title> {

    public Title findByCode(String code) {
        return find("code", code).firstResult();
    }

    public List<Title> listSorted() {
        return listAll(Sort.ascending("description"));
    }

    public Title findByCodeExcludingId(String code, Long excludeId) {
        return find("code = ?1 and id != ?2", code, excludeId).firstResult();
    }

    public Title findByDescriptionExcludingId(String description, Long excludeId) {
        return find("description = ?1 and id != ?2", description, excludeId).firstResult();
    }

    public Title findByDescription(String description) {
        return find("description", description).firstResult();
    }
}