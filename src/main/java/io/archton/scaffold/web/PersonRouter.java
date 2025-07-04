package io.archton.scaffold.web;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.domain.Title;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.service.PersonService;
import io.archton.scaffold.service.GenderService;
import io.archton.scaffold.service.TitleService;
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
public class PersonRouter extends BaseEntityRouter<Person> {

    private static final Logger log = Logger.getLogger(PersonRouter.class);

    @Inject
    PersonService personService;

    @Inject
    GenderService genderService;

    @Inject
    TitleService titleService;



    @CheckedTemplate(basePath = "person")
    public static class Templates {
        public static native TemplateInstance person(List<Person> persons, Person person, String errorMessage, List<Title> titles, List<Gender> genders);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /persons-ui");
        List<Person> personList = personService.listSorted();
        List<Title> titleList = titleService.listSorted();
        List<Gender> genderList = genderService.listSorted();
        return Templates.person(personList, null, null, titleList, genderList).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonTable() {
        log.debug("GET /persons-ui/table");
        List<Person> personList = personService.listSorted();
        String html = Templates.person(personList, null, null, null, null).getFragment("table").data("persons", personList).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonView(@PathParam("id") Long id) {
        log.debugf("GET /persons-ui/%s/view", id);

        Optional<Person> personOpt = personService.findByIdOptional(id);
        if (personOpt.isEmpty()) {
            throw new EntityNotFoundException("Person", id);
        }

        String html = Templates.person(null, personOpt.get(), null, null, null).getFragment("view").data("person", personOpt.get()).render();
        return Response.ok(html).build();
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonCreate() {
        log.debug("GET /persons-ui/create");

        List<Title> titleList = titleService.listSorted();
        List<Gender> genderList = genderService.listSorted();
        String html = Templates.person(null, null, null, titleList, genderList).getFragment("create").data("person", new Person()).data("errorMessage", null).data("titles", titleList).data("genders", genderList).render();
        return Response.ok(html).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createPersonFromForm(@FormParam("firstName") String firstName,
                                         @FormParam("lastName") String lastName,
                                         @FormParam("email") String email,
                                         @FormParam("titleId") Long titleId,
                                         @FormParam("genderId") Long genderId) {
        log.debugf("POST /persons-ui - create with email: %s", email);

        Person person = new Person();
        person.firstName = firstName;
        person.lastName = lastName;
        person.email = email;

        try {
            // Set relationships inside try block to catch relationship lookup errors
            if (titleId != null) {
                person.title = titleService.findById(titleId);
            }
            if (genderId != null) {
                person.gender = genderService.findById(genderId);
            }

            Person createdPerson = personService.createPerson(person);
            log.debugf("Person created successfully with ID: %s", createdPerson.id);

            // Success - return to table view
            List<Person> personList = personService.listSorted();
            String html = Templates.person(personList, null, null, null, null).getFragment("table").data("persons", personList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            // Error - re-render form with preserved data and error message
            // Store the form parameters in a temporary person for form data preservation
            Person formPerson = new Person();
            formPerson.firstName = firstName;
            formPerson.lastName = lastName;
            formPerson.email = email;
            // For error display, store the IDs for dropdown selection
            if (titleId != null) {
                Title tempTitle = new Title();
                tempTitle.id = titleId;
                formPerson.title = tempTitle;
            }
            if (genderId != null) {
                Gender tempGender = new Gender();
                tempGender.id = genderId;
                formPerson.gender = tempGender;
            }
            return handleEntityFormException(e, formPerson, "creating person", "create");
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonEdit(@PathParam("id") Long id) {
        log.debugf("GET /persons-ui/%s/edit", id);

        Optional<Person> personOpt = personService.findByIdOptional(id);
        if (personOpt.isEmpty()) {
            throw new EntityNotFoundException("Person", id);
        }

        List<Title> titleList = titleService.listSorted();
        List<Gender> genderList = genderService.listSorted();
        String html = Templates.person(null, personOpt.get(), null, titleList, genderList).getFragment("edit").data("person", personOpt.get()).data("errorMessage", null).data("titles", titleList).data("genders", genderList).render();
        return Response.ok(html).build();
    }

    @POST
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updatePersonFromForm(@PathParam("id") Long id,
                                         @FormParam("firstName") String firstName,
                                         @FormParam("lastName") String lastName,
                                         @FormParam("email") String email,
                                         @FormParam("titleId") Long titleId,
                                         @FormParam("genderId") Long genderId) {
        log.debugf("POST /persons-ui/%s/edit - update with email: %s", id, email);

        Person updates = new Person();
        updates.id = id; // Set ID for template rendering
        updates.firstName = firstName;
        updates.lastName = lastName;
        updates.email = email;

        try {
            // Set relationships inside try block to catch relationship lookup errors
            if (titleId != null) {
                updates.title = titleService.findById(titleId);
            }
            if (genderId != null) {
                updates.gender = genderService.findById(genderId);
            }

            Person updatedPerson = personService.updatePerson(id, updates);
            log.debugf("Person updated successfully with ID: %s", updatedPerson.id);

            // Success - return to table view
            List<Person> personList = personService.listSorted();
            String html = Templates.person(personList, null, null, null, null).getFragment("table").data("persons", personList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            // Error - re-render form with preserved data and error message
            // For error display, store the IDs for dropdown selection
            if (titleId != null) {
                Title tempTitle = new Title();
                tempTitle.id = titleId;
                updates.title = tempTitle;
            }
            if (genderId != null) {
                Gender tempGender = new Gender();
                tempGender.id = genderId;
                updates.gender = tempGender;
            }
            return handleEntityFormException(e, updates, "updating person " + id, "edit");
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonDelete(@PathParam("id") Long id) {
        log.debugf("GET /persons-ui/%s/delete", id);

        Optional<Person> personOpt = personService.findByIdOptional(id);
        if (personOpt.isEmpty()) {
            throw new EntityNotFoundException("Person", id);
        }

        String html = Templates.person(null, personOpt.get(), null, null, null).getFragment("delete").data("person", personOpt.get()).render();
        return Response.ok(html).build();
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

            // Success - return to table view
            List<Person> personList = personService.listSorted();
            String html = Templates.person(personList, null, null, null, null).getFragment("table").data("persons", personList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            return handleEntityDeleteException(e, "deleting person " + id);
        }
    }

    @Override
    protected String renderFragment(String fragmentName, Person entity, String errorMessage) {
        List<Person> personList = personService.listSorted();
        List<Title> titleList = titleService.listSorted();
        List<Gender> genderList = genderService.listSorted();
        return Templates.person(personList, entity, errorMessage, titleList, genderList)
            .getFragment(fragmentName)
            .data("person", entity)
            .data("errorMessage", errorMessage)
            .data("titles", titleList)
            .data("genders", genderList)
            .render();
    }

    @Override
    protected String renderTableWithError(String errorMessage) {
        List<Person> personList = personService.listSorted();
        return Templates.person(personList, null, errorMessage, null, null)
            .getFragment("table")
            .data("persons", personList)
            .data("errorMessage", errorMessage)
            .render();
    }
}