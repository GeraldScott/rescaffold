package io.archton.scaffold.web;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.repository.GenderRepository;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/genders-ui")
public class GenderTemplate {

    @Inject
    Template genders;
    @Inject
    GenderRepository genderRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getGenders() {
        // Create a model with data for the template
        Map<String, Object> model = new HashMap<>();
        model.put("activeNav", "genders");

        // Get all genders from the repository
        model.put("genders", genderRepository.listSorted());

        return genders.data(model).render();
    }
}