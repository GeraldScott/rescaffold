package io.archton.scaffold.web;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.service.GenderService;
import io.archton.scaffold.util.WebErrorHandler;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

@Path("/genders-ui")
public class GenderRouter {

    private static final Logger log = Logger.getLogger(GenderRouter.class);

    @Inject
    GenderService genderService;


    @CheckedTemplate(basePath = "gender")
    public static class Templates {
        public static native TemplateInstance gender(List<Gender> genders, Gender gender, String errorMessage);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /genders-ui");
        List<Gender> genderList = genderService.listSorted();
        return Templates.gender(genderList, null, null).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderTable() {
        log.debug("GET /genders-ui/table");
        List<Gender> genderList = genderService.listSorted();
        String html = Templates.gender(genderList, null, null).getFragment("table").data("genders", genderList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderView(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%s/view", id);

        try {
            Optional<Gender> genderOpt = genderService.findByIdOptional(id);
            if (genderOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.gender(null, genderOpt.get(), null).getFragment("view").data("gender", genderOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving gender for view", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderCreate() {
        log.debug("GET /genders-ui/create");

        String html = Templates.gender(null, null, null).getFragment("create").data("gender", new Gender()).data("errorMessage", null).render();
        return Response.ok(html).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createGenderFromForm(@FormParam("code") String code,
                                         @FormParam("description") String description) {
        log.debugf("POST /genders-ui - create with code: %s", code);

        try {
            Gender gender = new Gender();
            gender.code = code;
            gender.description = description;

            genderService.createGender(gender);

            List<Gender> genderList = genderService.listSorted();
            String html = Templates.gender(genderList, null, null).getFragment("table").data("genders", genderList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error creating gender", e);
            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            // Create entity with submitted form data to preserve user input
            Gender formData = new Gender();
            formData.code = code;
            formData.description = description;
            
            String html = Templates.gender(null, formData, errorMessage).getFragment("create").data("gender", formData).data("errorMessage", errorMessage).render();
            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderEdit(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%s/edit", id);

        try {
            Optional<Gender> genderOpt = genderService.findByIdOptional(id);
            if (genderOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.gender(null, genderOpt.get(), null).getFragment("edit").data("gender", genderOpt.get()).data("errorMessage", null).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving gender for edit", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateGenderFromForm(@PathParam("id") Long id,
                                         @FormParam("code") String code,
                                         @FormParam("description") String description) {
        log.debugf("PUT /genders-ui/%s - update with code: %s", id, code);

        try {
            Gender updateGender = new Gender();
            updateGender.code = code;
            updateGender.description = description;

            genderService.updateGender(id, updateGender);

            List<Gender> genderList = genderService.listSorted();
            String html = Templates.gender(genderList, null, null).getFragment("table").data("genders", genderList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error updating gender", e);
            
            // Create entity with submitted form data to preserve user input
            Gender formData = new Gender();
            formData.id = id;
            formData.code = code;
            formData.description = description;
            
            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            String html = Templates.gender(null, formData, errorMessage).getFragment("edit").data("gender", formData).data("errorMessage", errorMessage).render();
            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderDelete(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%s/delete", id);

        try {
            Optional<Gender> genderOpt = genderService.findByIdOptional(id);
            if (genderOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.gender(null, genderOpt.get(), null).getFragment("delete").data("gender", genderOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving gender for delete", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteGenderFromForm(@PathParam("id") Long id) {
        log.debugf("DELETE /genders-ui/%s", id);

        try {
            genderService.deleteGender(id);

            List<Gender> genderList = genderService.listSorted();
            String html = Templates.gender(genderList, null, null).getFragment("table").data("genders", genderList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error deleting gender", e);
            if (e instanceof IllegalArgumentException && e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
