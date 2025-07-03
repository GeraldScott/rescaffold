package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Country;
import io.archton.scaffold.service.CountryService;
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

@Path("/api/countries")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Country", description = "Country management operations")
public class CountryResource {

    private static final Logger log = Logger.getLogger(CountryResource.class);

    @Inject
    CountryService countryService;

    private Map<String, String> createErrorResponse(String message) {
        return Map.of("error", message);
    }

    @GET
    @Operation(summary = "Get all countries", description = "Retrieves an unsorted list of all countries")
    @APIResponse(responseCode = "200", description = "List of countries retrieved successfully")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getAllCountries() {
        log.debug("GET /api/countries");
        try {
            List<Country> countries = countryService.listAll();
            return Response.ok(countries).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get country by ID", description = "Retrieves a specific country by its ID")
    @APIResponse(responseCode = "200", description = "Country found")
    @APIResponse(responseCode = "404", description = "Country not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getCountryById(@Parameter(description = "Country ID") @PathParam("id") Long id) {
        log.debugf("GET /api/countries/%s", id);
        try {
            Country country = countryService.findById(id);
            if (country == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with id: " + id))
                        .build();
            }
            return Response.ok(country).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/code/{code}")
    @Operation(summary = "Get country by code", description = "Retrieves a specific country by its code")
    @APIResponse(responseCode = "200", description = "Country found")
    @APIResponse(responseCode = "404", description = "Country not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getCountryByCode(@Parameter(description = "Country code") @PathParam("code") String code) {
        log.debugf("GET /api/countries/code/%s", code);
        try {
            Country country = countryService.findByCode(code);
            if (country == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Entity not found with code: " + code))
                        .build();
            }
            return Response.ok(country).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Operation(summary = "Create a new country", description = "Creates a new country record")
    @APIResponse(responseCode = "201", description = "Country created successfully")
    @APIResponse(responseCode = "400", description = "Bad request: invalid input data")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response createCountry(@Valid Country country) {
        log.debugf("POST /api/countries - create with code: %s", country.code);

        try {
            Country created = countryService.createCountry(country);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating country: " + e.getMessage());
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
    @Operation(summary = "Update an existing country", description = "Updates an existing country record")
    @APIResponse(responseCode = "200", description = "Country updated successfully")
    @APIResponse(responseCode = "404", description = "Country not found")
    @APIResponse(responseCode = "409", description = "Conflicts with existing data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response updateCountry(@Parameter(description = "Country ID") @PathParam("id") Long id, @Valid Country newCountry) {
        log.debugf("PUT /api/countries/%s - update with code: %s", id, newCountry.code);

        try {
            Country updated = countryService.updateCountry(id, newCountry);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            log.error("Error updating country: " + e.getMessage());
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
    @Operation(summary = "Delete a country", description = "Deletes an existing country record")
    @APIResponse(responseCode = "204", description = "Country deleted successfully")
    @APIResponse(responseCode = "404", description = "Country not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response deleteCountry(@Parameter(description = "Country ID") @PathParam("id") Long id) {
        log.debugf("DELETE /api/countries/%s", id);

        try {
            countryService.deleteCountry(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting country: " + e.getMessage());
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