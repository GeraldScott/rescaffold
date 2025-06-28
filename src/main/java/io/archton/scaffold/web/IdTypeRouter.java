package io.archton.scaffold.web;

import io.archton.scaffold.domain.IdType;
import io.archton.scaffold.service.IdTypeService;
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
        public static native TemplateInstance idtypes(List<IdType> idTypes);

        public static native TemplateInstance table(List<IdType> idTypes);

        public static native TemplateInstance view(IdType idType);

        public static native TemplateInstance create();

        public static native TemplateInstance edit(IdType idType);

        public static native TemplateInstance delete(IdType idType);

    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /id-types-ui");
        List<IdType> idTypeList = idTypeService.listSorted();
        return Templates.idtypes(idTypeList).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getIdTypeTable() {
        log.debug("GET /id-types-ui/table");
        List<IdType> idTypeList = idTypeService.listSorted();
        String html = Templates.table(idTypeList).render();
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

            String html = Templates.view(idTypeOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving id type for view: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getIdTypeCreate() {
        log.debug("GET /id-types-ui/create");

        String html = Templates.create().render();
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
            String html = Templates.table(idTypeList).render();
            return Response.ok(html).build();
        } catch (IllegalArgumentException e) {
            log.error("Error creating id type: " + e.getMessage());
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating id type: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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

            String html = Templates.edit(idTypeOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving id type for edit: " + e.getMessage());
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
            String html = Templates.table(idTypeList).render();
            return Response.ok(html).build();
        } catch (IllegalArgumentException e) {
            log.error("Error updating id type: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating id type: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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

            String html = Templates.delete(idTypeOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving id type for delete: " + e.getMessage());
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
            String html = Templates.table(idTypeList).render();
            return Response.ok(html).build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting id type: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error deleting id type: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}