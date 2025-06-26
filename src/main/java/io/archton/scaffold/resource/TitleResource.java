package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Title;
import io.archton.scaffold.service.TitleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@Path("/api/titles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Title", description = "Title management operations")
public class TitleResource {

    private static final Logger log = Logger.getLogger(TitleResource.class);

    @Inject
    TitleService titleService;

    private Map<String, String> createErrorResponse(String message) {
        return Map.of("error", message);
    }

    @GET
    @Operation(summary = "Get all titles", description = "Retrieves an unsorted list of all titles")
    @APIResponse(responseCode = "200", description = "List of titles retrieved successfully")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getAllTitles() {
        log.debug("GET /api/titles");
        try {
            List<Title> titles = titleService.listAll();
            return Response.ok(titles).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get title by ID", description = "Retrieves a specific title by its ID")
    @APIResponse(responseCode = "200", description = "Title found")
    @APIResponse(responseCode = "404", description = "Title not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getTitleById(@Parameter(description = "Title ID") @PathParam("id") Long id) {
        log.debugf("GET /api/titles/%s", id);
        try {
            Title title = titleService.findById(id);
            if (title == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with id: " + id))
                        .build();
            }
            return Response.ok(title).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/code/{code}")
    @Operation(summary = "Get title by code", description = "Retrieves a specific title by its code")
    @APIResponse(responseCode = "200", description = "Title found")
    @APIResponse(responseCode = "404", description = "Title not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getTitleByCode(@Parameter(description = "Title code") @PathParam("code") String code) {
        log.debugf("GET /api/titles/code/%s", code);
        try {
            Title title = titleService.findByCode(code);
            if (title == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with code: " + code))
                        .build();
            }
            return Response.ok(title).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Operation(summary = "Create a new title", description = "Creates a new title record")
    @APIResponse(responseCode = "201", description = "Title created successfully")
    @APIResponse(responseCode = "400", description = "Bad request: invalid input data")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response createTitle(@Valid Title title) {
        log.debugf("POST /api/titles - create with code: %s", title.code);

        try {
            Title created = titleService.createTitle(title);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating title: " + e.getMessage());
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(createErrorResponse(e.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an existing title", description = "Updates an existing title record")
    @APIResponse(responseCode = "200", description = "Title updated successfully")
    @APIResponse(responseCode = "404", description = "Title not found")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response updateTitle(@Parameter(description = "Title ID") @PathParam("id") Long id, @Valid Title newTitle) {
        log.debugf("PUT /api/titles/%s - update with code: %s", id, newTitle.code);

        try {
            Title updated = titleService.updateTitle(id, newTitle);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            log.error("Error updating title: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse(e.getMessage()))
                        .build();
            }
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(createErrorResponse(e.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a title", description = "Deletes an existing title record")
    @APIResponse(responseCode = "204", description = "Title deleted successfully")
    @APIResponse(responseCode = "404", description = "Title not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response deleteTitle(@Parameter(description = "Title ID") @PathParam("id") Long id) {
        log.debugf("DELETE /api/titles/%s", id);

        try {
            titleService.deleteTitle(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting title: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }
}