package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Gender;
import jakarta.transaction.Transactional;
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
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \""+e.getMessage()+"\"}")
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
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Gender not found with id: " + id + "\"}")
                        .build();
            }
            return Response.ok(gender).build();
        } catch (Exception e) {
            log.error("Error retrieving gender by ID: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to retrieve gender\"}")
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
                        .entity("{\"error\": \"Gender not found with code: " + code + "\"}")
                        .build();
            }
            return Response.ok(gender).build();
        } catch (Exception e) {
            log.error("Error retrieving gender by code: " + code, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to retrieve gender\"}")
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
        try {
            // Check if gender with same code already exists
            Gender existingByCode = Gender.find("code", gender.code).firstResult();
            if (existingByCode != null) {
                log.infof("Attempt to create gender with existing code: %s", gender.code);
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Gender with code '" + gender.code + "' already exists\"}")
                        .build();
            }

            // Check if gender with same description already exists
            Gender existingByDescription = Gender.find("description", gender.description).firstResult();
            if (existingByDescription != null) {
                log.infof("Attempt to create gender with existing description: %s", gender.description);
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Gender with description '" + gender.description + "' already exists\"}")
                        .build();
            }

            gender.persist();
            log.infof("Created new gender with code: %s, id: %d", gender.code, gender.id);
            return Response.status(Response.Status.CREATED).entity(gender).build();

        } catch (jakarta.validation.ConstraintViolationException e) {
            log.warn("Validation error when creating gender", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid gender data: validation failed\"}")
                    .build();
        } catch (jakarta.persistence.PersistenceException e) {
            log.error("Database error when creating gender", e);
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Could not create gender due to database constraints\"}")
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error when creating gender", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to create gender\"}")
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
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Gender not found with id: " + id + "\"}")
                        .build();
            }

            existingGender.code = updatedGender.code;
            existingGender.description = updatedGender.description;
            existingGender.persist();

            return Response.ok(existingGender).build();

        } catch (Exception e) {
            log.error("Unexpected error when updating gender with ID: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to update gender\"}")
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
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Gender not found with id: " + id + "\"}")
                        .build();
            }

            gender.delete();
            return Response.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting gender with ID: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to delete gender\"}")
                    .build();
        }
    }
}
