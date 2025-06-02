package io.archton.scaffold.web;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.repository.GenderRepository;
import io.archton.scaffold.resource.GenderResource;
import io.archton.scaffold.util.TemplateConfig;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/genders-ui")
public class GenderTemplate {

    private static final Logger log = Logger.getLogger(GenderResource.class);

    @Inject
    GenderRepository genderRepository;

    @Inject
    TemplateConfig templateConfig;

    @CheckedTemplate(basePath = "gender")
    public static class Templates {
        public static native TemplateInstance genders(List<Gender> genders, int currentYear, String applicationVersion);
        public static native TemplateInstance view(Gender gender, int currentYear, String applicationVersion);
        public static native TemplateInstance table(List<Gender> genders, int currentYear, String applicationVersion);
        public static native TemplateInstance edit(Gender gender, int currentYear, String applicationVersion);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /api/genders");
        List<Gender> genderList = genderRepository.listSorted();
        return Templates.genders(genderList, templateConfig.getCurrentYear(), templateConfig.getApplicationVersion()).render();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderView(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%d/view", id);

        Gender gender = Gender.findById(id);
        if (gender == null) {
            // Return 404 or redirect back to list
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String html = Templates.view(gender, templateConfig.getCurrentYear(), templateConfig.getApplicationVersion()).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderTable() {
        log.debug("GET /genders-ui/table");
        List<Gender> genderList = genderRepository.listSorted();
        String html = Templates.table(genderList, templateConfig.getCurrentYear(), templateConfig.getApplicationVersion()).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderEdit(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%d/edit", id);

        Gender gender = Gender.findById(id);
        if (gender == null) {
            // Return 404 or redirect back to list
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String html = Templates.edit(gender, templateConfig.getCurrentYear(), templateConfig.getApplicationVersion()).render();
        return Response.ok(html).build();
    }
}
