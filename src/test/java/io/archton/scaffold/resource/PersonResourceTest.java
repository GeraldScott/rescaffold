package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.domain.Title;
import io.archton.scaffold.domain.IdType;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("Person Resource REST API Tests")
class PersonResourceTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    @BeforeEach
    void setUp() {
        // Common request specification for all tests
        requestSpec = new RequestSpecBuilder()
                .setBasePath("/api/persons")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        // Common response specification for successful responses
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }

    // Helper methods for creating test data
    private Person createValidPerson(String firstName, String lastName, String email) {
        Person person = new Person();
        person.firstName = firstName;
        person.lastName = lastName;
        person.email = email;
        return person;
    }

    private Person createPersonWithRelations() {
        // Find or create related entities
        Gender gender = Gender.findByCode("F");
        if (gender == null) {
            gender = new Gender("F", "Female");
            gender.persist();
        }

        Title title = Title.findByCode("MS");
        if (title == null) {
            title = new Title("MS", "Ms.");
            title.persist();
        }

        IdType idType = IdType.findByCode("ID");
        if (idType == null) {
            idType = new IdType("ID", "Identity Document");
            idType.persist();
        }

        Person person = new Person();
        person.firstName = "Jane";
        person.lastName = "Doe";
        person.email = "jane.doe.relations@example.com";
        person.idNumber = "1234567890";
        person.gender = gender;
        person.title = title;
        person.idType = idType;
        return person;
    }

    private Person createInvalidPersonWithBlankLastName() {
        Person person = new Person();
        person.firstName = "John";
        person.lastName = ""; // Invalid: cannot be blank
        person.email = "john@example.com";
        return person;
    }

    private Person createInvalidPersonWithInvalidEmail() {
        Person person = new Person();
        person.firstName = "John";
        person.lastName = "Smith";
        person.email = "invalid-email"; // Invalid email format
        return person;
    }

    // CRUD operation tests

    @Test
    @TestTransaction
    @DisplayName("GET /api/persons - Should return list successfully")
    void testGetAllPersons_Successfully() {
        given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should create valid person successfully")
    void testCreatePerson_ValidData() {
        Person person = createValidPerson("John", "Doe", "john.doe@example.com");

        given()
                .spec(requestSpec)
                .body(person)
                .when()
                .post()
                .then()
                .spec(responseSpec)
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("email", equalTo("john.doe@example.com"))
                .body("createdAt", notNullValue())
                .body("createdBy", equalTo("system"));
    }

    // @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should create person with foreign key relationships")
    void testCreatePerson_WithRelations() {
        Person person = createPersonWithRelations();

        given()
                .spec(requestSpec)
                .body(person)
                .when()
                .post()
                .then()
                .spec(responseSpec)
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName", equalTo("Jane"))
                .body("lastName", equalTo("Doe"))
                .body("email", equalTo("jane.doe.relations@example.com"))
                .body("idNumber", equalTo("1234567890"))
                .body("gender.id", notNullValue())
                .body("title.id", notNullValue())
                .body("idType.id", notNullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should reject person with blank last name")
    void testCreatePerson_BlankLastName() {
        Person person = createInvalidPersonWithBlankLastName();

        given()
                .spec(requestSpec)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should reject person with invalid email")
    void testCreatePerson_InvalidEmail() {
        Person person = createInvalidPersonWithInvalidEmail();

        given()
                .spec(requestSpec)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/persons/{id} - Should return person by ID")
    void testGetPersonById_Found() {
        // First create a person via API
        Person person = createValidPerson("Alice", "Johnson", "alice.johnson.found@example.com");
        Integer createdId = given()
                .spec(requestSpec)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then retrieve it by ID
        given()
                .spec(requestSpec)
                .when()
                .get("/{id}", createdId)
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("id", equalTo(createdId))
                .body("firstName", equalTo("Alice"))
                .body("lastName", equalTo("Johnson"))
                .body("email", equalTo("alice.johnson.found@example.com"));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/persons/{id} - Should return 404 for non-existent person")
    void testGetPersonById_NotFound() {
        given()
                .spec(requestSpec)
                .when()
                .get("/{id}", 99999L)
                .then()
                .statusCode(404);
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/persons/{id} - Should update person successfully")
    void testUpdatePerson_ValidData() {
        // First create a person via API
        Person person = createValidPerson("Bob", "Wilson", "bob.wilson.update@example.com");
        Integer createdId = given()
                .spec(requestSpec)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Update data
        person.firstName = "Robert";
        person.email = "robert.wilson.updated@example.com";

        given()
                .spec(requestSpec)
                .body(person)
                .when()
                .put("/{id}", createdId)
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("firstName", equalTo("Robert"))
                .body("lastName", equalTo("Wilson"))
                .body("email", equalTo("robert.wilson.updated@example.com"))
                .body("updatedAt", notNullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/persons/{id} - Should return 404 for non-existent person")
    void testUpdatePerson_NotFound() {
        Person person = createValidPerson("Nonexistent", "Person", "none@example.com");

        given()
                .spec(requestSpec)
                .body(person)
                .when()
                .put("/{id}", 99999L)
                .then()
                .statusCode(404);
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/persons/{id} - Should delete person")
    void testDeletePerson_HardDelete() {
        // First create a person via API
        Person person = createValidPerson("Charlie", "Brown", "charlie.brown.delete@example.com");
        Integer createdId = given()
                .spec(requestSpec)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .spec(requestSpec)
                .when()
                .delete("/{id}", createdId)
                .then()
                .statusCode(204);

        // Verify person is deleted
        given()
                .spec(requestSpec)
                .when()
                .get("/{id}", createdId)
                .then()
                .statusCode(404);
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/persons/{id} - Should return 404 for non-existent person")
    void testDeletePerson_NotFound() {
        given()
                .spec(requestSpec)
                .when()
                .delete("/{id}", 99999L)
                .then()
                .statusCode(404);
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/persons - Should return list of persons with created data")
    void testGetAllPersons_WithData() {
        // Get initial count
        int initialCount = given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$").size();

        // Create multiple persons via API
        Person person1 = createValidPerson("David", "Smith", "david.smith.list2@example.com");
        given()
                .spec(requestSpec)
                .body(person1)
                .when()
                .post()
                .then()
                .statusCode(201);

        Person person2 = createValidPerson("Emma", "Davis", "emma.davis.list2@example.com");
        given()
                .spec(requestSpec)
                .body(person2)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Verify that we now have the initial count plus our 2 new persons
        given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("$", hasSize(initialCount + 2));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should handle duplicate email constraint")
    void testCreatePerson_DuplicateEmail() {
        // Create first person with email via API
        Person person1 = createValidPerson("First", "Person", "duplicate.unique@example.com");
        given()
                .spec(requestSpec)
                .body(person1)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Try to create second person with same email
        Person person2 = createValidPerson("Second", "Person", "duplicate.unique@example.com");

        given()
                .spec(requestSpec)
                .body(person2)
                .when()
                .post()
                .then()
                .statusCode(409); // Conflict due to unique constraint
    }
}