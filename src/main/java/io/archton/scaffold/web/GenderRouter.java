package io.archton.scaffold.web;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.repository.GenderRepository;
import io.archton.scaffold.resource.GenderResource;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@Path("/genders-ui")
public class GenderRouter {

    private static final Logger log = Logger.getLogger(GenderRouter.class);

    @Inject
    GenderRepository genderRepository;

    @Inject
    GenderResource genderResource;


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

        Response apiResponse = genderResource.getGenderById(id);
        if (apiResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            return Response.status(apiResponse.getStatus()).build();
        }

        Gender gender = (Gender) apiResponse.getEntity();
        String html = Templates.view(gender).render();
        return Response.ok(html).build();
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

        Gender gender = new Gender();
        gender.code = code.trim().toUpperCase();
        gender.description = description.trim();

        Response apiResponse = genderResource.createGender(gender);
        if (apiResponse.getStatus() != Response.Status.CREATED.getStatusCode()) {
            return Response.status(apiResponse.getStatus()).build();
        }

        List<Gender> genderList = genderRepository.listSorted();
        String html = Templates.table(genderList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderEdit(@PathParam("id") UUID id) {
        log.debugf("GET /genders-ui/%s/edit", id);

        Response apiResponse = genderResource.getGenderById(id);
        if (apiResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            return Response.status(apiResponse.getStatus()).build();
        }

        Gender gender = (Gender) apiResponse.getEntity();
        String html = Templates.edit(gender).render();
        return Response.ok(html).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateGenderFromForm(@PathParam("id") UUID id,
                                         @FormParam("code") String code,
                                         @FormParam("description") String description) {
        log.debugf("PUT /genders-ui/%s - update with code: %s", id, code);

        Gender updateGender = new Gender();
        if (code != null && !code.trim().isEmpty()) {
            updateGender.code = code.trim().toUpperCase();
        }
        if (description != null && !description.trim().isEmpty()) {
            updateGender.description = description.trim();
        }

        Response apiResponse = genderResource.updateGender(id, updateGender);
        if (apiResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            return Response.status(apiResponse.getStatus()).build();
        }

        List<Gender> genderList = genderRepository.listSorted();
        String html = Templates.table(genderList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderDelete(@PathParam("id") UUID id) {
        log.debugf("GET /genders-ui/%s/delete", id);

        Response apiResponse = genderResource.getGenderById(id);
        if (apiResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            return Response.status(apiResponse.getStatus()).build();
        }

        Gender gender = (Gender) apiResponse.getEntity();
        String html = Templates.delete(gender).render();
        return Response.ok(html).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteGenderFromForm(@PathParam("id") UUID id) {
        log.debugf("DELETE /genders-ui/%s", id);

        Response apiResponse = genderResource.deleteGender(id);
        if (apiResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            return Response.status(apiResponse.getStatus()).build();
        }

        List<Gender> genderList = genderRepository.listSorted();
        String html = Templates.table(genderList).render();
        return Response.ok(html).build();
    }

}
