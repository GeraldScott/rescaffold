package io.archton.scaffold.web;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.repository.GenderRepository;
import io.archton.scaffold.resource.GenderResource;
import io.archton.scaffold.util.TemplateConfig;
import io.archton.scaffold.util.TemplateConfig.TemplateVars;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/genders-ui")
public class GenderRouter {

    private static final Logger log = Logger.getLogger(GenderResource.class);

    @Inject
    GenderRepository genderRepository;

    @Inject
    TemplateConfig templateConfig;

    @CheckedTemplate(basePath = "gender")
    public static class Templates {
        public static native TemplateInstance genders(List<Gender> genders, TemplateVars templateVars);

        public static native TemplateInstance table(List<Gender> genders, TemplateVars templateVars);

        public static native TemplateInstance view(Gender gender, TemplateVars templateVars);

        public static native TemplateInstance create(TemplateVars templateVars);

        public static native TemplateInstance edit(Gender gender, TemplateVars templateVars);

        public static native TemplateInstance delete(Gender gender, TemplateVars templateVars);

    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /api/genders");
        List<Gender> genderList = genderRepository.listSorted();
        return Templates.genders(genderList, templateConfig.getTemplateVars()).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderTable() {
        log.debug("GET /genders-ui/table");
        List<Gender> genderList = genderRepository.listSorted();
        String html = Templates.table(genderList, templateConfig.getTemplateVars()).render();
        return Response.ok(html).build();
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

        String html = Templates.view(gender, templateConfig.getTemplateVars()).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderCreate() {
        log.debug("GET /genders-ui/create");

        String html = Templates.create(templateConfig.getTemplateVars()).render();
        return Response.ok(html).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response createGenderFromForm(@FormParam("code") String code,
                                         @FormParam("description") String description) {
        log.debugf("POST /genders-ui - create with code: %s", code);

        try {
            if (code == null || code.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (description == null || description.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            // Check for duplicate code
            Gender existingWithCode = Gender.find("code", code.trim().toUpperCase()).firstResult();
            if (existingWithCode != null) {
                // Return error - you could create an error template or return to create with error
                return Response.status(Response.Status.CONFLICT).build();
            }

            // Check for duplicate description
            Gender existingWithDesc = Gender.find("description", description.trim()).firstResult();
            if (existingWithDesc != null) {
                // Return error - you could create an error template or return to create with error
                return Response.status(Response.Status.CONFLICT).build();
            }

            // Create new gender
            Gender gender = new Gender();
            gender.code = code.trim().toUpperCase();
            gender.description = description.trim();
            gender.persist();

            // Return updated table
            List<Gender> genderList = genderRepository.listSorted();
            String html = Templates.table(genderList, templateConfig.getTemplateVars()).render();
            return Response.ok(html).build();

        } catch (Exception e) {
            log.error("Error creating gender: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
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

        String html = Templates.edit(gender, templateConfig.getTemplateVars()).render();
        return Response.ok(html).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateGenderFromForm(@PathParam("id") Long id,
                                         @FormParam("code") String code,
                                         @FormParam("description") String description) {
        log.debugf("PUT /genders-ui/%d - update with code: %s", id, code);

        try {
            Gender gender = Gender.findById(id);
            if (gender == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Update fields
            if (code != null && !code.trim().isEmpty()) {
                // Check for duplicate code
                Gender existingWithCode = Gender.find("code = ?1 and id != ?2", code, id).firstResult();
                if (existingWithCode != null) {
                    // Return error - you could create an error template or return to edit with error
                    return Response.status(Response.Status.CONFLICT).build();
                }
                gender.code = code.trim().toUpperCase();
            }

            if (description != null && !description.trim().isEmpty()) {
                // Check for duplicate description
                Gender existingWithDesc = Gender.find("description = ?1 and id != ?2", description, id).firstResult();
                if (existingWithDesc != null) {
                    // Return error - you could create an error template or return to edit with error
                    return Response.status(Response.Status.CONFLICT).build();
                }
                gender.description = description.trim();
            }

            gender.persist();

            // Return updated table
            List<Gender> genderList = genderRepository.listSorted();
            String html = Templates.table(genderList, templateConfig.getTemplateVars()).render();
            return Response.ok(html).build();

        } catch (Exception e) {
            log.error("Error updating gender: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderDelete(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%d/delete", id);

        Gender gender = Gender.findById(id);
        if (gender == null) {
            // Return 404 or redirect back to list
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String html = Templates.delete(gender, templateConfig.getTemplateVars()).render();
        return Response.ok(html).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response deleteGenderFromForm(@PathParam("id") Long id) {
        log.debugf("DELETE /genders-ui/%d", id);

        try {
            Gender gender = Gender.findById(id);
            if (gender == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            gender.delete();

            // Return updated table
            List<Gender> genderList = genderRepository.listSorted();
            String html = Templates.table(genderList, templateConfig.getTemplateVars()).render();
            return Response.ok(html).build();

        } catch (Exception e) {
            log.error("Error deleting gender: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
