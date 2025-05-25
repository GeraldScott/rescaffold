package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.dto.ErrorResponse;
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
import org.jboss.logging.Logger;

import java.util.List;

@Path("/api/genders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Gender", description = "Gender management operations")
public class GenderResource {

    private static final Logger log = Logger.getLogger(GenderResource.class);

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
            log.error("GET /api/genders - Error retrieving genders", e);
            ErrorResponse errorResponse = ErrorResponse.withException(
                "Internal Server Error",
                "Failed to retrieve genders",
                e
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get gender by ID", description = "Retrieves a specific gender by its ID")
    @APIResponse(responseCode = "200", description = "Gender found")
    @APIResponse(responseCode = "404", description = "Gender not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getGenderById(@Parameter(description = "Gender ID") @PathParam("id") Long id) {
        log.debugf("GET /api/genders/%d", id);
        try {
            Gender gender = Gender.findById(id);
            if (gender == null) {
                ErrorResponse errorResponse = ErrorResponse.simple(
                    "Not Found",
                    "Gender not found with id: " + id
                );
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorResponse)
                        .build();
            }
            return Response.ok(gender).build();
        } catch (Exception e) {
            log.errorf(e, "GET /api/genders/%d - Error retrieving gender", id);
            ErrorResponse errorResponse = ErrorResponse.withException(
                "Internal Server Error",
                "Failed to retrieve gender",
                "with id: " + id,
                e
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
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
                ErrorResponse errorResponse = ErrorResponse.simple(
                    "Not Found",
                    "Gender not found with code: " + code
                );
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorResponse)
                        .build();
            }
            return Response.ok(gender).build();
        } catch (Exception e) {
            log.errorf(e, "GET /api/genders/code/%s - Error retrieving gender", code);
            ErrorResponse errorResponse = ErrorResponse.withException(
                "Internal Server Error",
                "Failed to retrieve gender",
                "with code: " + code,
                e
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new gender", description = "Creates a new gender record")
    @APIResponse(responseCode = "201", description = "Gender created successfully")
    @APIResponse(responseCode = "400", description = "Invalid input data")
    @APIResponse(responseCode = "409", description = "Gender with this code or description already exists")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response createGender(@Valid Gender gender) {
        log.debugf("POST /api/genders - create with code: %s", gender.code);

        if (gender.id != null) {
            log.debugf("POST /api/genders - ID provided in create request: %d", gender.id);
            ErrorResponse errorResponse = ErrorResponse.simple(
                "Bad Request",
                "ID must not be provided in create request"
            );
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        try {
            gender.persist();
            return Response.status(Response.Status.CREATED).entity(gender).build();

        } catch (ConstraintViolationException e) {
            log.errorf(e, "POST /api/genders - constraint violation for code: %s", gender.code);
            ErrorResponse errorResponse = ErrorResponse.withException(
                "Constraint Violation",
                "Database constraint violation",
                "for code: " + (gender.code != null ? gender.code : "null"),
                e
            );
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorResponse)
                    .build();
        } catch (Exception e) {
            log.errorf(e, "POST /api/genders - error creating gender with code: %s", gender.code);
            ErrorResponse errorResponse = ErrorResponse.withException(
                "Internal Server Error",
                "Failed to create gender",
                "with code: " + (gender.code != null ? gender.code : "null"),
                e
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update an existing gender", description = "Updates an existing gender record")
    @APIResponse(responseCode = "200", description = "Gender updated successfully")
    @APIResponse(responseCode = "404", description = "Gender not found")
    @APIResponse(responseCode = "400", description = "Invalid input data")
    @APIResponse(responseCode = "409", description = "Gender with this code or description already exists")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response updateGender(@Parameter(description = "Gender ID") @PathParam("id") Long id, @Valid Gender updatedGender) {
        log.debugf("PUT /api/genders/%d - update with code: %s", id, updatedGender.code);

        try {
            Gender existingGender = Gender.findById(id);
            if (existingGender == null) {
                ErrorResponse errorResponse = ErrorResponse.simple(
                    "Not Found",
                    "Gender not found with id: " + id
                );
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorResponse)
                        .build();
            }

            existingGender.code = updatedGender.code;
            existingGender.description = updatedGender.description;
            existingGender.persist();

            return Response.ok(existingGender).build();

        } catch (ConstraintViolationException e) {
            log.errorf(e, "PUT /api/genders/%d - constraint violation for code: %s", id, updatedGender.code);
            ErrorResponse errorResponse = ErrorResponse.withException(
                "Constraint Violation",
                "Database constraint violation",
                "for id: " + id,
                e
            );
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorResponse)
                    .build();
        } catch (Exception e) {
            log.errorf(e, "PUT /api/genders/%d - error updating gender with code: %s", id, updatedGender.code);
            ErrorResponse errorResponse = ErrorResponse.withException(
                "Internal Server Error",
                "Failed to update gender",
                "with id: " + id,
                e
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
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
    public Response deleteGender(@Parameter(description = "Gender ID") @PathParam("id") Long id) {
        log.debugf("DELETE /api/genders/%d", id);

        try {
            Gender gender = Gender.findById(id);
            if (gender == null) {
                ErrorResponse errorResponse = ErrorResponse.simple(
                    "Not Found",
                    "Gender not found with id: " + id
                );
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorResponse)
                        .build();
            }

            gender.delete();
            return Response.noContent().build();
        } catch (Exception e) {
            log.errorf(e, "DELETE /api/genders/%d - error deleting gender", id);
            ErrorResponse errorResponse = ErrorResponse.withException(
                "Internal Server Error",
                "Failed to delete gender",
                "with id: " + id,
                e
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }
}