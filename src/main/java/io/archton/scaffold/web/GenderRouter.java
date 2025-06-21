package io.archton.scaffold.web;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.repository.GenderRepository;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/genders-ui")
public class GenderRouter {

    private static final Logger log = Logger.getLogger(GenderRouter.class);

    @Inject
    GenderRepository genderRepository;


    @CheckedTemplate(basePath = "gender")
    public static class Templates {
        public static native TemplateInstance genders(List<Gender> genders);

        public static native TemplateInstance table(List<Gender> genders);

        public static native TemplateInstance view(Gender gender);

        public static native TemplateInstance create();

        public static native TemplateInstance edit(Gender gender);

        public static native TemplateInstance delete(Gender gender);

    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /api/genders");
        List<Gender> genderList = genderRepository.listSorted();
        return Templates.genders(genderList).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderTable() {
        log.debug("GET /genders-ui/table");
        List<Gender> genderList = genderRepository.listSorted();
        String html = Templates.table(genderList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderView(@PathParam("id") UUID id) {
        log.debugf("GET /genders-ui/%s/view", id);

        try {
            Optional<Gender> genderOpt = genderRepository.findByIdOptional(id);
            if (genderOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.view(genderOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving gender for view: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderCreate() {
        log.debug("GET /genders-ui/create");

        String html = Templates.create().render();
        return Response.ok(html).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createGenderFromForm(@FormParam("code") String code,
                                         @FormParam("description") String description) {
        log.debugf("POST /genders-ui - create with code: %s", code);

        if (code == null || code.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            Gender gender = new Gender();
            gender.code = code.trim().toUpperCase();
            gender.description = description.trim();

            genderRepository.createGender(gender);

            List<Gender> genderList = genderRepository.listSorted();
            String html = Templates.table(genderList).render();
            return Response.ok(html).build();
        } catch (IllegalArgumentException e) {
            log.error("Error creating gender: " + e.getMessage());
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating gender: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderEdit(@PathParam("id") UUID id) {
        log.debugf("GET /genders-ui/%s/edit", id);

        try {
            Optional<Gender> genderOpt = genderRepository.findByIdOptional(id);
            if (genderOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.edit(genderOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving gender for edit: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateGenderFromForm(@PathParam("id") UUID id,
                                         @FormParam("code") String code,
                                         @FormParam("description") String description) {
        log.debugf("PUT /genders-ui/%s - update with code: %s", id, code);

        try {
            Gender updateGender = new Gender();
            if (code != null && !code.trim().isEmpty()) {
                updateGender.code = code.trim().toUpperCase();
            }
            if (description != null && !description.trim().isEmpty()) {
                updateGender.description = description.trim();
            }

            genderRepository.updateGender(id, updateGender);

            List<Gender> genderList = genderRepository.listSorted();
            String html = Templates.table(genderList).render();
            return Response.ok(html).build();
        } catch (IllegalArgumentException e) {
            log.error("Error updating gender: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating gender: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderDelete(@PathParam("id") UUID id) {
        log.debugf("GET /genders-ui/%s/delete", id);

        try {
            Optional<Gender> genderOpt = genderRepository.findByIdOptional(id);
            if (genderOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.delete(genderOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving gender for delete: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteGenderFromForm(@PathParam("id") UUID id) {
        log.debugf("DELETE /genders-ui/%s", id);

        try {
            genderRepository.deleteGender(id);

            List<Gender> genderList = genderRepository.listSorted();
            String html = Templates.table(genderList).render();
            return Response.ok(html).build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting gender: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error deleting gender: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
