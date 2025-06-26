package io.archton.scaffold.web;

import io.archton.scaffold.domain.Gender;
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
import java.util.Map;

@Path("/genders-ui")
public class GenderRouter {

    private static final Logger log = Logger.getLogger(GenderRouter.class);

    @Inject
    GenderService genderService;

    @CheckedTemplate(basePath = "gender")
    public static class Templates {
        // Main template for the full page
        public static native TemplateInstance genders(List<Gender> genders);

        // Fragment template
        public static native TemplateInstance genderCrud(List<Gender> genders, Gender gender, String mode);
    }

    // ========================================
    // MAIN ENDPOINTS
    // ========================================

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /genders-ui - full page");
        List<Gender> genderList = genderService.listSorted();
        return Templates.genders(genderList).render();
    }

    // ========================================
    // FRAGMENT ENDPOINTS
    // ========================================

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderTable() {
        log.debug("GET /genders-ui/table - clear form area");
        // Return empty content to clear the form area
        return Response.ok("").build();
    }

    @GET
    @Path("/refresh")
    @Produces(MediaType.TEXT_HTML)
    public Response refreshTable() {
        log.debug("GET /genders-ui/refresh - refresh table content");
        try {
            List<Gender> genderList = genderService.listSorted();

            // Return just the table fragment for the card body
            String html = Templates.genderCrud(genderList, null, "list")
                    .getFragment("gender_table")
                    .render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error refreshing gender table: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/rows")
    @Produces(MediaType.TEXT_HTML)
    public Response getTableRows() {
        log.debug("GET /genders-ui/rows - table rows fragment");
        try {
            List<Gender> genderList = genderService.listSorted();

            // Return just the table rows fragment
            String html = Templates.genderCrud(genderList, null, "list")
                    .getFragment("table_rows")
                    .render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving gender table rows: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderCreate() {
        log.debug("GET /genders-ui/create - create form fragment");

        // Return just the create form fragment
        String html = Templates.genderCrud(null, null, "create")
                .getFragment("create_form")
                .render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderView(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%s/view - view details fragment", id);

        try {
            Optional<Gender> genderOpt = genderService.findByIdOptional(id);
            if (genderOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Return just the view details fragment
            String html = Templates.genderCrud(null, genderOpt.get(), "view")
                    .getFragment("view_details")
                    .render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving gender for view: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderEdit(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%s/edit - edit form fragment", id);

        try {
            Optional<Gender> genderOpt = genderService.findByIdOptional(id);
            if (genderOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Return just the edit form fragment
            String html = Templates.genderCrud(null, genderOpt.get(), "edit")
                    .getFragment("edit_form")
                    .render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving gender for edit: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getGenderDelete(@PathParam("id") Long id) {
        log.debugf("GET /genders-ui/%s/delete - delete confirmation fragment", id);

        try {
            Optional<Gender> genderOpt = genderService.findByIdOptional(id);
            if (genderOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Return just the delete confirmation fragment
            String html = Templates.genderCrud(null, genderOpt.get(), "delete")
                    .getFragment("delete_confirm")
                    .render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving gender for delete: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // CRUD OPERATIONS
    // ========================================

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createGenderFromForm(@FormParam("code") String code,
                                         @FormParam("description") String description) {
        log.info("POST /genders-ui - create with code: " + code);

        // Validate input
        if (code == null || code.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorFragment("Gender code is required"))
                    .build();
        }

        if (!code.trim().matches("[A-Z]")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorFragment("Code must be a single uppercase alphabetic character"))
                    .build();
        }

        if (description == null || description.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorFragment("Gender description is required"))
                    .build();
        }

        try {
            Gender gender = new Gender();
            gender.code = code.trim();
            gender.description = description.trim();

            genderService.createGender(gender);

            // Return the entire updated table to replace the content
            List<Gender> genderList = genderService.listSorted();
            log.info("After create - Gender list size: " + genderList.size());
            for (Gender g : genderList) {
                log.info("Gender in list: " + g.code + " - " + g.description);
            }
            String html = Templates.genderCrud(genderList, null, "list")
                    .getFragment("gender_table")
                    .render();
            log.info("Generated HTML length: " + html.length());
            return Response.ok(html).build();

        } catch (IllegalArgumentException e) {
            log.error("Error creating gender: " + e.getMessage());

            // Return error response that HTMX can handle
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(createErrorFragment("Code or description already exists"))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorFragment(e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error("Error creating gender: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorFragment("An unexpected error occurred"))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateGenderFromForm(@PathParam("id") Long id,
                                         @FormParam("code") String code,
                                         @FormParam("description") String description,
                                         @FormParam("isActive") Boolean isActive) {
        log.debugf("PUT /genders-ui/%s - update with code: %s", id, code);

        // Validate input
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorFragment("Gender ID is required"))
                    .build();
        }

        if (code != null && !code.trim().isEmpty() && !code.trim().matches("[A-Z]")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorFragment("Code must be a single uppercase alphabetic character"))
                    .build();
        }

        try {
            Gender updateGender = new Gender();
            updateGender.code = code != null ? code.trim() : null;
            updateGender.description = description != null ? description.trim() : null;
            updateGender.isActive = isActive != null ? isActive : false;

            Gender updatedGender = genderService.updateGender(id, updateGender);

            // Return the entire updated table to replace the content
            List<Gender> genderList = genderService.listSorted();
            String html = Templates.genderCrud(genderList, null, "list")
                    .getFragment("gender_table")
                    .render();
            return Response.ok(html).build();

        } catch (IllegalArgumentException e) {
            log.error("Error updating gender: " + e.getMessage());

            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorFragment("Gender not found"))
                        .build();
            }
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(createErrorFragment("Code or description already exists"))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorFragment(e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error("Error updating gender: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorFragment("An unexpected error occurred"))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteGenderFromForm(@PathParam("id") Long id) {
        log.debugf("DELETE /genders-ui/%s", id);

        try {
            genderService.deleteGender(id);

            // Return the entire updated table to replace the content
            List<Gender> genderList = genderService.listSorted();
            String html = Templates.genderCrud(genderList, null, "list")
                    .getFragment("gender_table")
                    .render();
            return Response.ok(html).build();

        } catch (IllegalArgumentException e) {
            log.error("Error deleting gender: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorFragment("Gender not found"))
                    .build();
        } catch (Exception e) {
            log.error("Error deleting gender: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorFragment("An unexpected error occurred"))
                    .build();
        }
    }

    // ========================================
    // HELPER METHODS
    // ========================================


    /**
     * Creates an error fragment for form validation errors
     */
    private String createErrorFragment(String message) {
        // Escape the message to prevent XSS attacks
        String escapedMessage = escapeHtml(message);

        return String.format("""
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle"></i>
                %s
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            """, escapedMessage);
    }

    /**
     * Simple HTML escaping to prevent XSS attacks
     */
    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }

        Map<Character, String> htmlEscapes = Map.of(
            '&', "&amp;",
            '<', "&lt;",
            '>', "&gt;",
            '"', "&quot;",
            '\'', "&#39;"
        );

        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            String replacement = htmlEscapes.get(c);
            if (replacement != null) {
                escaped.append(replacement);
            } else {
                escaped.append(c);
            }
        }

        return escaped.toString();
    }
}
