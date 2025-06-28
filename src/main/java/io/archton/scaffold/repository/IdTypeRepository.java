package io.archton.scaffold.repository;

import io.archton.scaffold.domain.IdType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class IdTypeRepository implements PanacheRepository<IdType> {

    public IdType findByCode(String code) {
        return find("code", code).firstResult();
    }

    public List<IdType> listSorted() {
        return listAll(Sort.ascending("description"));
    }

    public IdType findByCodeExcludingId(String code, Long excludeId) {
        return find("code = ?1 and id != ?2", code, excludeId).firstResult();
    }

    public IdType findByDescriptionExcludingId(String description, Long excludeId) {
        return find("description = ?1 and id != ?2", description, excludeId).firstResult();
    }

    public IdType findByDescription(String description) {
        return find("description", description).firstResult();
    }
}