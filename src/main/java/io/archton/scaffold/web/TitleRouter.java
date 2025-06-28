package io.archton.scaffold.web;

import io.archton.scaffold.domain.Title;
import io.archton.scaffold.service.TitleService;
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

@Path("/titles-ui")
public class TitleRouter {

    private static final Logger log = Logger.getLogger(TitleRouter.class);

    @Inject
    TitleService titleService;


    @CheckedTemplate(basePath = "title")
    public static class Templates {
        public static native TemplateInstance titles(List<Title> titles);

        public static native TemplateInstance table(List<Title> titles);

        public static native TemplateInstance view(Title title);

        public static native TemplateInstance create(String errorMessage);

        public static native TemplateInstance edit(Title title, String errorMessage);

        public static native TemplateInstance delete(Title title);

    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /titles-ui");
        List<Title> titleList = titleService.listSorted();
        return Templates.titles(titleList).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleTable() {
        log.debug("GET /titles-ui/table");
        List<Title> titleList = titleService.listSorted();
        String html = Templates.table(titleList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleView(@PathParam("id") Long id) {
        log.debugf("GET /titles-ui/%s/view", id);

        try {
            Optional<Title> titleOpt = titleService.findByIdOptional(id);
            if (titleOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.view(titleOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving title for view", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleCreate() {
        log.debug("GET /titles-ui/create");

        String html = Templates.create(null).render();
        return Response.ok(html).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createTitleFromForm(@FormParam("code") String code,
                                       @FormParam("description") String description) {
        log.debugf("POST /titles-ui - create with code: %s", code);

        try {
            Title title = new Title();
            title.code = code;
            title.description = description;

            titleService.createTitle(title);

            List<Title> titleList = titleService.listSorted();
            String html = Templates.table(titleList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error creating title", e);
            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            String html = Templates.create(errorMessage).render();
            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleEdit(@PathParam("id") Long id) {
        log.debugf("GET /titles-ui/%s/edit", id);

        try {
            Optional<Title> titleOpt = titleService.findByIdOptional(id);
            if (titleOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.edit(titleOpt.get(), null).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving title for edit", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateTitleFromForm(@PathParam("id") Long id,
                                       @FormParam("code") String code,
                                       @FormParam("description") String description) {
        log.debugf("PUT /titles-ui/%s - update with code: %s", id, code);

        try {
            Title updateTitle = new Title();
            updateTitle.code = code;
            updateTitle.description = description;

            titleService.updateTitle(id, updateTitle);

            List<Title> titleList = titleService.listSorted();
            String html = Templates.table(titleList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error updating title", e);
            
            // Create entity with submitted form data to preserve user input
            Title formData = new Title();
            formData.id = id;
            formData.code = code;
            formData.description = description;
            
            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            String html = Templates.edit(formData, errorMessage).render();
            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleDelete(@PathParam("id") Long id) {
        log.debugf("GET /titles-ui/%s/delete", id);

        try {
            Optional<Title> titleOpt = titleService.findByIdOptional(id);
            if (titleOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.delete(titleOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving title for delete", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteTitleFromForm(@PathParam("id") Long id) {
        log.debugf("DELETE /titles-ui/%s", id);

        try {
            titleService.deleteTitle(id);

            List<Title> titleList = titleService.listSorted();
            String html = Templates.table(titleList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error deleting title", e);
            if (e instanceof IllegalArgumentException && e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}