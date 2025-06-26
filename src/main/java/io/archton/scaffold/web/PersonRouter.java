package io.archton.scaffold.web;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.domain.Title;
import io.archton.scaffold.domain.IdType;
import io.archton.scaffold.service.PersonService;
import io.archton.scaffold.service.GenderService;
import io.archton.scaffold.service.TitleService;
import io.archton.scaffold.service.IdTypeService;
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
        public static native TemplateInstance persons(List<Person> persons, List<Title> titles, List<Gender> genders, List<IdType> idTypes);

        public static native TemplateInstance table(List<Person> persons);

        public static native TemplateInstance view(Person person);

        public static native TemplateInstance create(List<Title> titles, List<Gender> genders, List<IdType> idTypes);

        public static native TemplateInstance edit(Person person, List<Title> titles, List<Gender> genders, List<IdType> idTypes);

        public static native TemplateInstance delete(Person person);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /persons-ui");
        List<Person> personList = personService.listSorted();
        List<Title> titleList = titleService.listSorted();
        List<Gender> genderList = genderService.listSorted();
        List<IdType> idTypeList = idTypeService.listSorted();
        return Templates.persons(personList, titleList, genderList, idTypeList).render();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public Response getPersonTable() {
        log.debug("GET /persons-ui/table");
        List<Person> personList = personService.listSorted();
        String html = Templates.table(personList).render();
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

            String html = Templates.view(personOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving person for view: " + e.getMessage());
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
        String html = Templates.create(titleList, genderList, idTypeList).render();
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
            String html = Templates.table(personList).render();
            return Response.ok(html).build();

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating person: " + e.getMessage());
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating person: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
            String html = Templates.edit(personOpt.get(), titleList, genderList, idTypeList).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving person for edit: " + e.getMessage());
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
                                         @FormParam("idTypeId") Long idTypeId,
                                         @FormParam("isActive") Boolean isActive) {
        log.debugf("POST /persons-ui/%s/edit - update with email: %s", id, email);

        try {
            Person updates = new Person();
            updates.firstName = firstName;
            updates.lastName = lastName;
            updates.email = email;
            updates.idNumber = idNumber;
            updates.isActive = isActive != null ? isActive : true;
            
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
            String html = Templates.table(personList).render();
            return Response.ok(html).build();

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating person: " + e.getMessage());
            Optional<Person> personOpt = personService.findByIdOptional(id);
            if (personOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            List<Title> titleList = titleService.listSorted();
            List<Gender> genderList = genderService.listSorted();
            List<IdType> idTypeList = idTypeService.listSorted();
            String html = Templates.edit(personOpt.get(), titleList, genderList, idTypeList).render();
            return Response.status(Response.Status.BAD_REQUEST).entity(html).build();
        } catch (Exception e) {
            log.error("Error updating person: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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

            String html = Templates.delete(personOpt.get()).render();
            return Response.ok(html).build();
        } catch (Exception e) {
            log.error("Error retrieving person for delete: " + e.getMessage());
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
            String html = Templates.table(personList).render();
            return Response.ok(html).build();

        } catch (IllegalArgumentException e) {
            log.error("Error deleting person: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error deleting person: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}