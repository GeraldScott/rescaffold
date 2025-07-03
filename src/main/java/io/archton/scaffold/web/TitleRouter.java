package io.archton.scaffold.web;

import io.archton.scaffold.domain.Title;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.service.TitleService;
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
public class TitleRouter extends BaseEntityRouter<Title> {

    private static final Logger log = Logger.getLogger(TitleRouter.class);

    @Inject
    TitleService titleService;



    @CheckedTemplate(basePath = "title")
    public static class Templates {
        public static native TemplateInstance title(List<Title> titles, Title title, String errorMessage);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /titles-ui");
        List<Title> titleList = titleService.listSorted();
        return Templates.title(titleList, null, null).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleTable() {
        log.debug("GET /titles-ui/table");
        List<Title> titleList = titleService.listSorted();
        String html = Templates.title(titleList, null, null).getFragment("table").data("titles", titleList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleView(@PathParam("id") Long id) {
        log.debugf("GET /titles-ui/%s/view", id);

        Optional<Title> titleOpt = titleService.findByIdOptional(id);
        if (titleOpt.isEmpty()) {
            throw new EntityNotFoundException("Title", id);
        }

        String html = Templates.title(null, titleOpt.get(), null).getFragment("view").data("title", titleOpt.get()).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleCreate() {
        log.debug("GET /titles-ui/create");

        String html = Templates.title(null, null, null).getFragment("create").data("title", new Title()).data("errorMessage", null).render();
        return Response.ok(html).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createTitleFromForm(@FormParam("code") String code,
                                       @FormParam("description") String description) {
        log.debugf("POST /titles-ui - create with code: %s", code);

        Title title = new Title();
        title.code = code;
        title.description = description;

        try {
            titleService.createTitle(title);
            
            // Success - return to table view
            List<Title> titleList = titleService.listSorted();
            String html = Templates.title(titleList, null, null).getFragment("table").data("titles", titleList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            // Error - re-render form with preserved data and error message
            return handleEntityFormException(e, title, "creating title", "create");
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleEdit(@PathParam("id") Long id) {
        log.debugf("GET /titles-ui/%s/edit", id);

        Optional<Title> titleOpt = titleService.findByIdOptional(id);
        if (titleOpt.isEmpty()) {
            throw new EntityNotFoundException("Title", id);
        }

        String html = Templates.title(null, titleOpt.get(), null).getFragment("edit").data("title", titleOpt.get()).data("errorMessage", null).render();
        return Response.ok(html).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateTitleFromForm(@PathParam("id") Long id,
                                       @FormParam("code") String code,
                                       @FormParam("description") String description) {
        log.debugf("PUT /titles-ui/%s - update with code: %s", id, code);

        Title updateTitle = new Title();
        updateTitle.id = id; // Set ID for template rendering
        updateTitle.code = code;
        updateTitle.description = description;

        try {
            titleService.updateTitle(id, updateTitle);
            
            // Success - return to table view
            List<Title> titleList = titleService.listSorted();
            String html = Templates.title(titleList, null, null).getFragment("table").data("titles", titleList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            // Error - re-render form with preserved data and error message
            return handleEntityFormException(e, updateTitle, "updating title " + id, "edit");
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getTitleDelete(@PathParam("id") Long id) {
        log.debugf("GET /titles-ui/%s/delete", id);

        Optional<Title> titleOpt = titleService.findByIdOptional(id);
        if (titleOpt.isEmpty()) {
            throw new EntityNotFoundException("Title", id);
        }

        String html = Templates.title(null, titleOpt.get(), null).getFragment("delete").data("title", titleOpt.get()).render();
        return Response.ok(html).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteTitleFromForm(@PathParam("id") Long id) {
        log.debugf("DELETE /titles-ui/%s", id);

        try {
            titleService.deleteTitle(id);
            List<Title> titleList = titleService.listSorted();
            String html = Templates.title(titleList, null, null).getFragment("table").data("titles", titleList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            return handleEntityDeleteException(e, "deleting title " + id);
        }
    }

    @Override
    protected String renderFragment(String fragmentName, Title entity, String errorMessage) {
        List<Title> titleList = titleService.listSorted();
        return Templates.title(titleList, entity, errorMessage)
            .getFragment(fragmentName)
            .data("title", entity)
            .data("errorMessage", errorMessage)
            .render();
    }

    @Override
    protected String renderTableWithError(String errorMessage) {
        List<Title> titleList = titleService.listSorted();
        return Templates.title(titleList, null, errorMessage)
            .getFragment("table")
            .data("titles", titleList)
            .data("errorMessage", errorMessage)
            .render();
    }

}