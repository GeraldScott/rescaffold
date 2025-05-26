package io.archton.scaffold.web;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.repository.GenderRepository;
import io.archton.scaffold.resource.GenderResource;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/genders-ui")
public class GenderTemplate {

    private static final Logger log = Logger.getLogger(GenderResource.class);

    @Inject
    GenderRepository genderRepository;

    @CheckedTemplate(basePath = "gender")
    public static class Templates {
        public static native TemplateInstance genders(List<Gender> genders);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /api/genders");
        List<Gender> genderList = genderRepository.listSorted();
        return Templates.genders(genderList).render();
    }
}
