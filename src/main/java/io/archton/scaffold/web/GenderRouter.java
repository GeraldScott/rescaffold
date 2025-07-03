package io.archton.scaffold.web;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.service.GenderService;
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
public class GenderRouter extends BaseEntityRouter<Gender> {

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

        Optional<Gender> genderOpt = genderService.findByIdOptional(id);
        if (genderOpt.isEmpty()) {
            throw new EntityNotFoundException("Gender", id);
        }

        String html = Templates.gender(null, genderOpt.get(), null).getFragment("view").data("gender", genderOpt.get()).render();
        return Response.ok(html).build();
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

        Gender gender = new Gender();
        gender.code = code;
        gender.description = description;

        try {
            genderService.createGender(gender);
            
            // Success - return to table view
            List<Gender> genderList = genderService.listSorted();
            String html = Templates.gender(genderList, null, null).getFragment("table").data("genders", genderList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            // Error - re-render form with preserved data and error message
            return handleEntityFormException(e, gender, "creating gender", "create");
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderEdit(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%s/edit", id);

        Optional<Gender> genderOpt = genderService.findByIdOptional(id);
        if (genderOpt.isEmpty()) {
            throw new EntityNotFoundException("Gender", id);
        }

        String html = Templates.gender(null, genderOpt.get(), null).getFragment("edit").data("gender", genderOpt.get()).data("errorMessage", null).render();
        return Response.ok(html).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateGenderFromForm(@PathParam("id") Long id,
                                         @FormParam("code") String code,
                                         @FormParam("description") String description) {
        log.debugf("PUT /genders-ui/%s - update with code: %s", id, code);

        Gender updateGender = new Gender();
        updateGender.id = id; // Set ID for template rendering
        updateGender.code = code;
        updateGender.description = description;

        try {
            genderService.updateGender(id, updateGender);
            
            // Success - return to table view
            List<Gender> genderList = genderService.listSorted();
            String html = Templates.gender(genderList, null, null).getFragment("table").data("genders", genderList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            // Error - re-render form with preserved data and error message
            return handleEntityFormException(e, updateGender, "updating gender " + id, "edit");
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderDelete(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%s/delete", id);

        Optional<Gender> genderOpt = genderService.findByIdOptional(id);
        if (genderOpt.isEmpty()) {
            throw new EntityNotFoundException("Gender", id);
        }

        String html = Templates.gender(null, genderOpt.get(), null).getFragment("delete").data("gender", genderOpt.get()).render();
        return Response.ok(html).build();
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
            return handleEntityDeleteException(e, "deleting gender " + id);
        }
    }

    @Override
    protected String renderFragment(String fragmentName, Gender entity, String errorMessage) {
        List<Gender> genderList = genderService.listSorted();
        return Templates.gender(genderList, entity, errorMessage)
            .getFragment(fragmentName)
            .data("gender", entity)
            .data("errorMessage", errorMessage)
            .render();
    }

    @Override
    protected String renderTableWithError(String errorMessage) {
        List<Gender> genderList = genderService.listSorted();
        return Templates.gender(genderList, null, errorMessage)
            .getFragment("table")
            .data("genders", genderList)
            .data("errorMessage", errorMessage)
            .render();
    }

}
