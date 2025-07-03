package io.archton.scaffold.web;

import io.archton.scaffold.domain.IdType;
import io.archton.scaffold.service.IdTypeService;
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

@Path("/id-types-ui")
public class IdTypeRouter {

    private static final Logger log = Logger.getLogger(IdTypeRouter.class);

    @Inject
    IdTypeService idTypeService;


    @CheckedTemplate(basePath = "idtype")
    public static class Templates {
        public static native TemplateInstance idtype(List<IdType> idTypes, IdType idType, String errorMessage);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /id-types-ui");
        List<IdType> idTypeList = idTypeService.listSorted();
        return Templates.idtype(idTypeList, null, null).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getIdTypeTable() {
        log.debug("GET /id-types-ui/table");
        List<IdType> idTypeList = idTypeService.listSorted();
        String html = Templates.idtype(idTypeList, null, null).getFragment("table").data("idTypes", idTypeList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getIdTypeView(@PathParam("id") Long id) {
        log.debugf("GET /id-types-ui/%s/view", id);

        try {
            Optional<IdType> idTypeOpt = idTypeService.findByIdOptional(id);
            if (idTypeOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.idtype(null, idTypeOpt.get(), null).getFragment("view").data("idType", idTypeOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving id type for view", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getIdTypeCreate() {
        log.debug("GET /id-types-ui/create");

        String html = Templates.idtype(null, null, null).getFragment("create").data("idType", new IdType()).data("errorMessage", null).render();
        return Response.ok(html).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createIdTypeFromForm(@FormParam("code") String code,
                                        @FormParam("description") String description) {
        log.debugf("POST /id-types-ui - create with code: %s", code);

        try {
            IdType idType = new IdType();
            idType.code = code;
            idType.description = description;

            idTypeService.createIdType(idType);

            List<IdType> idTypeList = idTypeService.listSorted();
            String html = Templates.idtype(idTypeList, null, null).getFragment("table").data("idTypes", idTypeList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error creating id type", e);
            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            // Create entity with submitted form data to preserve user input
            IdType formData = new IdType();
            formData.code = code;
            formData.description = description;
            
            String html = Templates.idtype(null, formData, errorMessage).getFragment("create").data("idType", formData).data("errorMessage", errorMessage).render();
            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getIdTypeEdit(@PathParam("id") Long id) {
        log.debugf("GET /id-types-ui/%s/edit", id);

        try {
            Optional<IdType> idTypeOpt = idTypeService.findByIdOptional(id);
            if (idTypeOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.idtype(null, idTypeOpt.get(), null).getFragment("edit").data("idType", idTypeOpt.get()).data("errorMessage", null).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving id type for edit", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateIdTypeFromForm(@PathParam("id") Long id,
                                        @FormParam("code") String code,
                                        @FormParam("description") String description) {
        log.debugf("PUT /id-types-ui/%s - update with code: %s", id, code);

        try {
            IdType updateIdType = new IdType();
            updateIdType.code = code;
            updateIdType.description = description;

            idTypeService.updateIdType(id, updateIdType);

            List<IdType> idTypeList = idTypeService.listSorted();
            String html = Templates.idtype(idTypeList, null, null).getFragment("table").data("idTypes", idTypeList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error updating id type", e);
            
            // Create entity with submitted form data to preserve user input
            IdType formData = new IdType();
            formData.id = id;
            formData.code = code;
            formData.description = description;
            
            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            String html = Templates.idtype(null, formData, errorMessage).getFragment("edit").data("idType", formData).data("errorMessage", errorMessage).render();
            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getIdTypeDelete(@PathParam("id") Long id) {
        log.debugf("GET /id-types-ui/%s/delete", id);

        try {
            Optional<IdType> idTypeOpt = idTypeService.findByIdOptional(id);
            if (idTypeOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.idtype(null, idTypeOpt.get(), null).getFragment("delete").data("idType", idTypeOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving id type for delete", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteIdTypeFromForm(@PathParam("id") Long id) {
        log.debugf("DELETE /id-types-ui/%s", id);

        try {
            idTypeService.deleteIdType(id);

            List<IdType> idTypeList = idTypeService.listSorted();
            String html = Templates.idtype(idTypeList, null, null).getFragment("table").data("idTypes", idTypeList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error deleting id type", e);
            if (e instanceof IllegalArgumentException && e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}