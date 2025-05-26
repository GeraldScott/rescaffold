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
}