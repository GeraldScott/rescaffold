package io.archton.scaffold.web;

import io.archton.scaffold.domain.Country;
import io.archton.scaffold.service.CountryService;
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

@Path("/countries-ui")
public class CountryRouter {

    private static final Logger log = Logger.getLogger(CountryRouter.class);

    @Inject
    CountryService countryService;


    @CheckedTemplate(basePath = "country")
    public static class Templates {
        public static native TemplateInstance country(List<Country> countries, Country country, String errorMessage);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /countries-ui");
        List<Country> countryList = countryService.listSorted();
        return Templates.country(countryList, null, null).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getCountryTable() {
        log.debug("GET /countries-ui/table");
        List<Country> countryList = countryService.listSorted();
        String html = Templates.country(countryList, null, null).getFragment("table").data("countries", countryList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getCountryView(@PathParam("id") Long id) {
        log.debugf("GET /countries-ui/%s/view", id);

        try {
            Optional<Country> countryOpt = countryService.findByIdOptional(id);
            if (countryOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.country(null, countryOpt.get(), null).getFragment("view").data("country", countryOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving country for view", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getCountryCreate() {
        log.debug("GET /countries-ui/create");

        String html = Templates.country(null, null, null).getFragment("create").data("country", new Country()).data("errorMessage", null).render();
        return Response.ok(html).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createCountryFromForm(@FormParam("code") String code,
                                         @FormParam("name") String name,
                                         @FormParam("year") String year,
                                         @FormParam("cctld") String cctld) {
        log.debugf("POST /countries-ui - create with code: %s", code);

        try {
            Country country = new Country();
            country.code = code;
            country.name = name;
            country.year = year;
            country.cctld = cctld;

            countryService.createCountry(country);

            List<Country> countryList = countryService.listSorted();
            String html = Templates.country(countryList, null, null).getFragment("table").data("countries", countryList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error creating country", e);
            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            // Create entity with submitted form data to preserve user input
            Country formData = new Country();
            formData.code = code;
            formData.name = name;
            formData.year = year;
            formData.cctld = cctld;
            
            String html = Templates.country(null, formData, errorMessage).getFragment("create").data("country", formData).data("errorMessage", errorMessage).render();
            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getCountryEdit(@PathParam("id") Long id) {
        log.debugf("GET /countries-ui/%s/edit", id);

        try {
            Optional<Country> countryOpt = countryService.findByIdOptional(id);
            if (countryOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.country(null, countryOpt.get(), null).getFragment("edit").data("country", countryOpt.get()).data("errorMessage", null).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving country for edit", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateCountryFromForm(@PathParam("id") Long id,
                                         @FormParam("code") String code,
                                         @FormParam("name") String name,
                                         @FormParam("year") String year,
                                         @FormParam("cctld") String cctld) {
        log.debugf("PUT /countries-ui/%s - update with code: %s", id, code);

        try {
            Country updateCountry = new Country();
            updateCountry.code = code;
            updateCountry.name = name;
            updateCountry.year = year;
            updateCountry.cctld = cctld;

            countryService.updateCountry(id, updateCountry);

            List<Country> countryList = countryService.listSorted();
            String html = Templates.country(countryList, null, null).getFragment("table").data("countries", countryList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error updating country", e);
            
            // Create entity with submitted form data to preserve user input
            Country formData = new Country();
            formData.id = id;
            formData.code = code;
            formData.name = name;
            formData.year = year;
            formData.cctld = cctld;
            
            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            String html = Templates.country(null, formData, errorMessage).getFragment("edit").data("country", formData).data("errorMessage", errorMessage).render();
            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getCountryDelete(@PathParam("id") Long id) {
        log.debugf("GET /countries-ui/%s/delete", id);

        try {
            Optional<Country> countryOpt = countryService.findByIdOptional(id);
            if (countryOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.country(null, countryOpt.get(), null).getFragment("delete").data("country", countryOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving country for delete", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteCountryFromForm(@PathParam("id") Long id) {
        log.debugf("DELETE /countries-ui/%s", id);

        try {
            countryService.deleteCountry(id);

            List<Country> countryList = countryService.listSorted();
            String html = Templates.country(countryList, null, null).getFragment("table").data("countries", countryList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error deleting country", e);
            if (e instanceof IllegalArgumentException && e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}