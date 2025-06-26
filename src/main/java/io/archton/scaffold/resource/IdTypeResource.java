package io.archton.scaffold.resource;

import io.archton.scaffold.domain.IdType;
import io.archton.scaffold.service.IdTypeService;
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

@Path("/api/id-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "IdType", description = "ID Type management operations")
public class IdTypeResource {

    private static final Logger log = Logger.getLogger(IdTypeResource.class);

    @Inject
    IdTypeService idTypeService;

    private Map<String, String> createErrorResponse(String message) {
        return Map.of("error", message);
    }

    @GET
    @Operation(summary = "Get all ID types", description = "Retrieves an unsorted list of all ID types")
    @APIResponse(responseCode = "200", description = "List of ID types retrieved successfully")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getAllIdTypes() {
        log.debug("GET /api/id-types");
        try {
            List<IdType> idTypes = idTypeService.listAll();
            return Response.ok(idTypes).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get ID type by ID", description = "Retrieves a specific ID type by its ID")
    @APIResponse(responseCode = "200", description = "ID type found")
    @APIResponse(responseCode = "404", description = "ID type not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getIdTypeById(@Parameter(description = "ID type ID") @PathParam("id") Long id) {
        log.debugf("GET /api/id-types/%s", id);
        try {
            IdType idType = idTypeService.findById(id);
            if (idType == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with id: " + id))
                        .build();
            }
            return Response.ok(idType).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/code/{code}")
    @Operation(summary = "Get ID type by code", description = "Retrieves a specific ID type by its code")
    @APIResponse(responseCode = "200", description = "ID type found")
    @APIResponse(responseCode = "404", description = "ID type not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getIdTypeByCode(@Parameter(description = "ID type code") @PathParam("code") String code) {
        log.debugf("GET /api/id-types/code/%s", code);
        try {
            IdType idType = idTypeService.findByCode(code);
            if (idType == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with code: " + code))
                        .build();
            }
            return Response.ok(idType).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Operation(summary = "Create a new ID type", description = "Creates a new ID type record")
    @APIResponse(responseCode = "201", description = "ID type created successfully")
    @APIResponse(responseCode = "400", description = "Bad request: invalid input data")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response createIdType(@Valid IdType idType) {
        log.debugf("POST /api/id-types - create with code: %s", idType.code);

        try {
            IdType created = idTypeService.createIdType(idType);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating ID type: " + e.getMessage());
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
    @Operation(summary = "Update an existing ID type", description = "Updates an existing ID type record")
    @APIResponse(responseCode = "200", description = "ID type updated successfully")
    @APIResponse(responseCode = "404", description = "ID type not found")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response updateIdType(@Parameter(description = "ID type ID") @PathParam("id") Long id, @Valid IdType newIdType) {
        log.debugf("PUT /api/id-types/%s - update with code: %s", id, newIdType.code);

        try {
            IdType updated = idTypeService.updateIdType(id, newIdType);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            log.error("Error updating ID type: " + e.getMessage());
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
    @Operation(summary = "Delete an ID type", description = "Deletes an existing ID type record")
    @APIResponse(responseCode = "204", description = "ID type deleted successfully")
    @APIResponse(responseCode = "404", description = "ID type not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response deleteIdType(@Parameter(description = "ID type ID") @PathParam("id") Long id) {
        log.debugf("DELETE /api/id-types/%s", id);

        try {
            idTypeService.deleteIdType(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting ID type: " + e.getMessage());
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