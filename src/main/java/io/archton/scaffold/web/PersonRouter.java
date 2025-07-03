package io.archton.scaffold.web;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.domain.Title;
import io.archton.scaffold.domain.IdType;
import io.archton.scaffold.exception.DuplicateEntityException;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.exception.ValidationException;
import io.archton.scaffold.service.PersonService;
import io.archton.scaffold.service.GenderService;
import io.archton.scaffold.service.TitleService;
import io.archton.scaffold.service.IdTypeService;
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

@Path("/persons-ui")
public class PersonRouter {

    private static final Logger log = Logger.getLogger(PersonRouter.class);

    @Inject
    PersonService personService;

    @Inject
    GenderService genderService;

    @Inject
    TitleService titleService;

    @Inject
    IdTypeService idTypeService;

    @CheckedTemplate(basePath = "person")
    public static class Templates {
        public static native TemplateInstance person(List<Person> persons, Person person, String errorMessage, List<Title> titles, List<Gender> genders, List<IdType> idTypes);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /persons-ui");
        List<Person> personList = personService.listSorted();
        List<Title> titleList = titleService.listSorted();
        List<Gender> genderList = genderService.listSorted();
        List<IdType> idTypeList = idTypeService.listSorted();
        return Templates.person(personList, null, null, titleList, genderList, idTypeList).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonTable() {
        log.debug("GET /persons-ui/table");
        List<Person> personList = personService.listSorted();
        String html = Templates.person(personList, null, null, null, null, null).getFragment("table").data("persons", personList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonView(@PathParam("id") Long id) {
        log.debugf("GET /persons-ui/%s/view", id);

        try {
            Optional<Person> personOpt = personService.findByIdOptional(id);
            if (personOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.person(null, personOpt.get(), null, null, null, null).getFragment("view").data("person", personOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving person for view", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonCreate() {
        log.debug("GET /persons-ui/create");

        List<Title> titleList = titleService.listSorted();
        List<Gender> genderList = genderService.listSorted();
        List<IdType> idTypeList = idTypeService.listSorted();
        String html = Templates.person(null, null, null, titleList, genderList, idTypeList).getFragment("create").data("person", new Person()).data("errorMessage", null).data("titles", titleList).data("genders", genderList).data("idTypes", idTypeList).render();
        return Response.ok(html).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createPersonFromForm(@FormParam("firstName") String firstName,
                                         @FormParam("lastName") String lastName,
                                         @FormParam("email") String email,
                                         @FormParam("idNumber") String idNumber,
                                         @FormParam("titleId") Long titleId,
                                         @FormParam("genderId") Long genderId,
                                         @FormParam("idTypeId") Long idTypeId) {
        log.debugf("POST /persons-ui - create with email: %s", email);

        try {
            Person person = new Person();
            person.firstName = firstName;
            person.lastName = lastName;
            person.email = email;
            person.idNumber = idNumber;

            // Set relationships
            if (titleId != null) {
                person.title = titleService.findById(titleId);
            }
            if (genderId != null) {
                person.gender = genderService.findById(genderId);
            }
            if (idTypeId != null) {
                person.idType = idTypeService.findById(idTypeId);
            }

            Person createdPerson = personService.createPerson(person);
            log.debugf("Person created successfully with ID: %s", createdPerson.id);

            // Return the updated table directly instead of redirecting
            List<Person> personList = personService.listSorted();
            String html = Templates.person(personList, null, null, null, null, null).getFragment("table").data("persons", personList).render();
            return Response.ok(html).build();

        } catch (Exception e) {
            WebErrorHandler.logError("Error creating person", e);
            
            List<Title> titleList = titleService.listSorted();
            List<Gender> genderList = genderService.listSorted();
            List<IdType> idTypeList = idTypeService.listSorted();

            // Create a temporary person object to preserve entered data
            Person enteredData = new Person();
            enteredData.firstName = firstName;
            enteredData.lastName = lastName;
            enteredData.email = email;
            enteredData.idNumber = idNumber;

            // Set relationships to preserve selected dropdowns
            if (titleId != null) {
                try {
                    enteredData.title = titleService.findById(titleId);
                } catch (Exception ex) {
                    log.warn("Could not find title with ID: " + titleId);
                }
            }
            if (genderId != null) {
                try {
                    enteredData.gender = genderService.findById(genderId);
                } catch (Exception ex) {
                    log.warn("Could not find gender with ID: " + genderId);
                }
            }
            if (idTypeId != null) {
                try {
                    enteredData.idType = idTypeService.findById(idTypeId);
                } catch (Exception ex) {
                    log.warn("Could not find idType with ID: " + idTypeId);
                }
            }

            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            String html = Templates.person(null, enteredData, errorMessage, titleList, genderList, idTypeList).getFragment("create").data("person", enteredData).data("errorMessage", errorMessage).data("titles", titleList).data("genders", genderList).data("idTypes", idTypeList).render();

            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonEdit(@PathParam("id") Long id) {
        log.debugf("GET /persons-ui/%s/edit", id);

        try {
            Optional<Person> personOpt = personService.findByIdOptional(id);
            if (personOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            List<Title> titleList = titleService.listSorted();
            List<Gender> genderList = genderService.listSorted();
            List<IdType> idTypeList = idTypeService.listSorted();
            String html = Templates.person(null, personOpt.get(), null, titleList, genderList, idTypeList).getFragment("edit").data("person", personOpt.get()).data("errorMessage", null).data("titles", titleList).data("genders", genderList).data("idTypes", idTypeList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving person for edit", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updatePersonFromForm(@PathParam("id") Long id,
                                         @FormParam("firstName") String firstName,
                                         @FormParam("lastName") String lastName,
                                         @FormParam("email") String email,
                                         @FormParam("idNumber") String idNumber,
                                         @FormParam("titleId") Long titleId,
                                         @FormParam("genderId") Long genderId,
                                         @FormParam("idTypeId") Long idTypeId) {
        log.debugf("POST /persons-ui/%s/edit - update with email: %s", id, email);

        try {
            Person updates = new Person();
            updates.firstName = firstName;
            updates.lastName = lastName;
            updates.email = email;
            updates.idNumber = idNumber;

            // Set relationships
            if (titleId != null) {
                updates.title = titleService.findById(titleId);
            }
            if (genderId != null) {
                updates.gender = genderService.findById(genderId);
            }
            if (idTypeId != null) {
                updates.idType = idTypeService.findById(idTypeId);
            }

            Person updatedPerson = personService.updatePerson(id, updates);
            log.debugf("Person updated successfully with ID: %s", updatedPerson.id);

            // Return the updated table directly instead of redirecting
            List<Person> personList = personService.listSorted();
            String html = Templates.person(personList, null, null, null, null, null).getFragment("table").data("persons", personList).render();
            return Response.ok(html).build();

        } catch (Exception e) {
            WebErrorHandler.logError("Error updating person", e);

            // Get the original person record
            Optional<Person> personOpt = personService.findByIdOptional(id);
            if (personOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Update the person with form values to preserve input
            Person person = personOpt.get();
            person.firstName = firstName;
            person.lastName = lastName;
            person.email = email;
            person.idNumber = idNumber;

            // Set relationships
            if (titleId != null) {
                try {
                    person.title = titleService.findById(titleId);
                } catch (Exception ex) {
                    log.warn("Could not find title with ID: " + titleId);
                }
            } else {
                person.title = null;
            }

            if (genderId != null) {
                try {
                    person.gender = genderService.findById(genderId);
                } catch (Exception ex) {
                    log.warn("Could not find gender with ID: " + genderId);
                }
            } else {
                person.gender = null;
            }

            if (idTypeId != null) {
                try {
                    person.idType = idTypeService.findById(idTypeId);
                } catch (Exception ex) {
                    log.warn("Could not find idType with ID: " + idTypeId);
                }
            } else {
                person.idType = null;
            }

            List<Title> titleList = titleService.listSorted();
            List<Gender> genderList = genderService.listSorted();
            List<IdType> idTypeList = idTypeService.listSorted();

            String errorMessage = WebErrorHandler.getUserFriendlyMessage(e);
            String html = Templates.person(null, person, errorMessage, titleList, genderList, idTypeList).getFragment("edit").data("person", person).data("errorMessage", errorMessage).data("titles", titleList).data("genders", genderList).data("idTypes", idTypeList).render();

            return WebErrorHandler.createErrorResponse(html, e);
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonDelete(@PathParam("id") Long id) {
        log.debugf("GET /persons-ui/%s/delete", id);

        try {
            Optional<Person> personOpt = personService.findByIdOptional(id);
            if (personOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String html = Templates.person(null, personOpt.get(), null, null, null, null).getFragment("delete").data("person", personOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error retrieving person for delete", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deletePersonFromForm(@PathParam("id") Long id) {
        log.debugf("POST /persons-ui/%s/delete", id);

        try {
            personService.deletePerson(id);
            log.debugf("Person soft deleted successfully with ID: %s", id);

            // Return the updated table directly instead of redirecting
            List<Person> personList = personService.listSorted();
            String html = Templates.person(personList, null, null, null, null, null).getFragment("table").data("persons", personList).render();
            return Response.ok(html).build();

        } catch (EntityNotFoundException e) {
            WebErrorHandler.logError("Error deleting person", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            WebErrorHandler.logError("Error deleting person", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}