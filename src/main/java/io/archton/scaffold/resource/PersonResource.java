package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.exception.ValidationException;
import io.archton.scaffold.service.PersonService;
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

@Path("/api/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Person", description = "Person management operations")
public class PersonResource {

    private static final Logger log = Logger.getLogger(PersonResource.class);

    @Inject
    PersonService personService;

    private Map<String, String> createErrorResponse(String message) {
        return Map.of("error", message);
    }

    @GET
    @Operation(summary = "Get all persons", description = "Retrieves a list of all persons sorted by last name")
    @APIResponse(responseCode = "200", description = "List of persons retrieved successfully")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getAllPersons() {
        log.debug("GET /api/persons");
        try {
            List<Person> persons = personService.listSorted();
            return Response.ok(persons).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get person by ID", description = "Retrieves a specific person by their ID")
    @APIResponse(responseCode = "200", description = "Person found")
    @APIResponse(responseCode = "404", description = "Person not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getPersonById(@Parameter(description = "Person ID") @PathParam("id") Long id) {
        log.debugf("GET /api/persons/%s", id);
        try {
            Person person = personService.findById(id);
            if (person == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with id: " + id))
                        .build();
            }
            return Response.ok(person).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/email/{email}")
    @Operation(summary = "Get person by email", description = "Retrieves a specific person by their email address")
    @APIResponse(responseCode = "200", description = "Person found")
    @APIResponse(responseCode = "404", description = "Person not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getPersonByEmail(@Parameter(description = "Person email") @PathParam("email") String email) {
        log.debugf("GET /api/persons/email/%s", email);
        try {
            Person person = personService.findByEmail(email);
            if (person == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with email: " + email))
                        .build();
            }
            return Response.ok(person).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Operation(summary = "Create a new person", description = "Creates a new person record")
    @APIResponse(responseCode = "201", description = "Person created successfully")
    @APIResponse(responseCode = "400", description = "Bad request: invalid input data")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response createPerson(@Valid Person person) {
        log.debugf("POST /api/persons - create with email: %s", person.email);

        try {
            Person created = personService.createPerson(person);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (ValidationException e) {
            log.error("Validation error creating person: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (DuplicateEntityException e) {
            log.error("Person with email '" + e.getFieldValue() + "' already exists");
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

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an existing person", description = "Updates an existing person record")
    @APIResponse(responseCode = "200", description = "Person updated successfully")
    @APIResponse(responseCode = "404", description = "Person not found")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response updatePerson(@Parameter(description = "Person ID") @PathParam("id") Long id, @Valid Person newPerson) {
        log.debugf("PUT /api/persons/%s - update with email: %s", id, newPerson.email);

        try {
            Person updated = personService.updatePerson(id, newPerson);
            return Response.ok(updated).build();
        } catch (EntityNotFoundException e) {
            log.error("Person not found with id: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (ValidationException e) {
            log.error("Validation error updating person: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        } catch (DuplicateEntityException e) {
            log.error("Person with email '" + e.getFieldValue() + "' already exists");
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
    @Operation(summary = "Delete a person", description = "Deletes an existing person record")
    @APIResponse(responseCode = "204", description = "Person deleted successfully")
    @APIResponse(responseCode = "404", description = "Person not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response deletePerson(@Parameter(description = "Person ID") @PathParam("id") Long id) {
        log.debugf("DELETE /api/persons/%s", id);

        try {
            personService.deletePerson(id);
            return Response.noContent().build();
        } catch (EntityNotFoundException e) {
            log.error("Person not found with id: " + id);
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