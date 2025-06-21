package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Gender;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.hibernate.exception.DataException;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/genders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Gender", description = "Gender management operations")
public class GenderResource {

    private static final Logger log = Logger.getLogger(GenderResource.class);

    private Map<String, String> createErrorResponse(String message) {
        return Map.of("error", message);
    }

    @GET
    @Operation(summary = "Get all genders", description = "Retrieves an unsorted list of all genders")
    @APIResponse(responseCode = "200", description = "List of genders retrieved successfully")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getAllGenders() {
        log.debug("GET /api/genders");
        try {
            List<Gender> genders = Gender.listAll();
            return Response.ok(genders).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get gender by ID", description = "Retrieves a specific gender by its ID")
    @APIResponse(responseCode = "200", description = "Gender found")
    @APIResponse(responseCode = "404", description = "Gender not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getGenderById(@Parameter(description = "Gender ID") @PathParam("id") UUID id) {
        log.debugf("GET /api/genders/%s", id);
        try {
            Gender gender = Gender.findById(id);
            if (gender == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with id: " + id))
                        .build();
            }
            return Response.ok(gender).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/code/{code}")
    @Operation(summary = "Get gender by code", description = "Retrieves a specific gender by its code")
    @APIResponse(responseCode = "200", description = "Gender found")
    @APIResponse(responseCode = "404", description = "Gender not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getGenderByCode(@Parameter(description = "Gender code") @PathParam("code") String code) {
        log.debugf("GET /api/genders/code/%s", code);
        try {
            Gender gender = Gender.find("code", code).firstResult();
            if (gender == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with code: " + code))
                        .build();
            }
            return Response.ok(gender).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new gender", description = "Creates a new gender record")
    @APIResponse(responseCode = "201", description = "Gender created successfully")
    @APIResponse(responseCode = "400", description = "Bad request: invalid input data")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response createGender(@Valid Gender gender) {
        log.debugf("POST /api/genders - create with code: %s", gender.code);

        try {
            if (gender.id != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("ID must not be included in POST request"))
                        .build();
            }
            if (Gender.find("code", gender.code).firstResult() != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(createErrorResponse("Gender with code '" + gender.code + "' already exists"))
                        .build();
            }
            if (Gender.find("description", gender.description).firstResult() != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(createErrorResponse("Gender with description '" + gender.description + "' already exists"))
                        .build();
            }
            gender.persist();
            return Response.status(Response.Status.CREATED).entity(gender).build();

        } catch (DataException | ConstraintViolationException e) {
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
    @Transactional
    @Operation(summary = "Update an existing gender", description = "Updates an existing gender record")
    @APIResponse(responseCode = "200", description = "Gender updated successfully")
    @APIResponse(responseCode = "404", description = "Gender not found")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response updateGender(@Parameter(description = "Gender ID") @PathParam("id") UUID id, @Valid Gender newGender) {
        log.debugf("PUT /api/genders/%s - update with code: %s", id, newGender.code);

        try {
            Gender gender = Gender.findById(id);
            if (gender == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with id: " + id))
                        .build();
            }
            if (newGender.code != null){
                if (Gender.find("code = ?1 and id != ?2", newGender.code, id).firstResult() != null) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(createErrorResponse("Another gender with code '" + newGender.code + "' already exists"))
                            .build();
                } else {
                    gender.code = newGender.code;
                }
            }
            if (newGender.description != null){
                if (Gender.find("description = ?1 and id != ?2", newGender.description, id).firstResult() != null) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(createErrorResponse("Another gender with description '" + newGender.description + "' already exists"))
                            .build();
                } else {
                    gender.description = newGender.description;
                }
            }

            gender.persist();
            return Response.ok(gender).build();

        } catch (DataException | ConstraintViolationException e) {
            return Response.status(Response.Status.CONFLICT)
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
    @Transactional
    @Operation(summary = "Delete a gender", description = "Deletes an existing gender record")
    @APIResponse(responseCode = "204", description = "Gender deleted successfully")
    @APIResponse(responseCode = "404", description = "Gender not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response deleteGender(@Parameter(description = "Gender ID") @PathParam("id") UUID id) {
        log.debugf("DELETE /api/genders/%s", id);

        try {
            Gender gender = Gender.findById(id);
            if (gender == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with id: " + id))
                        .build();
            }

            gender.delete();
            return Response.noContent().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }
}
