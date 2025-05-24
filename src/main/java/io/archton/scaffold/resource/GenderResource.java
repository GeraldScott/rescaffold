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

import java.util.List;

@Path("/api/genders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Gender", description = "Gender management operations")
public class GenderResource {

    @GET
    @Operation(summary = "Get all genders", description = "Retrieves a list of all genders")
    @APIResponse(responseCode = "200", description = "List of genders retrieved successfully")
    public List<Gender> getAllGenders() {
        return Gender.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get gender by ID", description = "Retrieves a specific gender by its ID")
    @APIResponse(responseCode = "200", description = "Gender found")
    @APIResponse(responseCode = "404", description = "Gender not found")
    public Response getGenderById(@Parameter(description = "Gender ID") @PathParam("id") Long id) {
        Gender gender = Gender.findById(id);
        if (gender == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Gender not found with id: " + id + "\"}")
                    .build();
        }
        return Response.ok(gender).build();
    }

    @GET
    @Path("/code/{code}")
    @Operation(summary = "Get gender by code", description = "Retrieves a specific gender by its code")
    @APIResponse(responseCode = "200", description = "Gender found")
    @APIResponse(responseCode = "404", description = "Gender not found")
    public Response getGenderByCode(@Parameter(description = "Gender code") @PathParam("code") String code) {
        Gender gender = Gender.find("code", code).firstResult();
        if (gender == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Gender not found with code: " + code + "\"}")
                    .build();
        }
        return Response.ok(gender).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new gender", description = "Creates a new gender record")
    @APIResponse(responseCode = "201", description = "Gender created successfully")
    @APIResponse(responseCode = "400", description = "Invalid input data")
    @APIResponse(responseCode = "409", description = "Gender with this code or description already exists")
    public Response createGender(@Valid Gender gender) {
        try {
            // Check if gender with same code already exists
            Gender existingByCode = Gender.find("code", gender.code).firstResult();
            if (existingByCode != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Gender with code '" + gender.code + "' already exists\"}")
                        .build();
            }

            // Check if gender with same description already exists
            Gender existingByDescription = Gender.find("description", gender.description).firstResult();
            if (existingByDescription != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Gender with description '" + gender.description + "' already exists\"}")
                        .build();
            }

            gender.persist();
            return Response.status(Response.Status.CREATED).entity(gender).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Failed to create gender: " + e.getMessage() + "\"}")
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
    public Response updateGender(@Parameter(description = "Gender ID") @PathParam("id") Long id, @Valid Gender updatedGender) {
        try {
            Gender existingGender = Gender.findById(id);
            if (existingGender == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Gender not found with id: " + id + "\"}")
                        .build();
            }

            // Check if another gender with same code already exists (excluding current one)
            Gender existingByCode = Gender.find("code = ?1 and id != ?2", updatedGender.code, id).firstResult();
            if (existingByCode != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Another gender with code '" + updatedGender.code + "' already exists\"}")
                        .build();
            }

            // Check if another gender with same description already exists (excluding current one)
            Gender existingByDescription = Gender.find("description = ?1 and id != ?2", updatedGender.description, id).firstResult();
            if (existingByDescription != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Another gender with description '" + updatedGender.description + "' already exists\"}")
                        .build();
            }

            existingGender.code = updatedGender.code;
            existingGender.description = updatedGender.description;
            existingGender.persist();

            return Response.ok(existingGender).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Failed to update gender: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete a gender", description = "Deletes an existing gender record")
    @APIResponse(responseCode = "204", description = "Gender deleted successfully")
    @APIResponse(responseCode = "404", description = "Gender not found")
    public Response deleteGender(@Parameter(description = "Gender ID") @PathParam("id") Long id) {
        Gender gender = Gender.findById(id);
        if (gender == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Gender not found with id: " + id + "\"}")
                    .build();
        }

        gender.delete();
        return Response.noContent().build();
    }
}
