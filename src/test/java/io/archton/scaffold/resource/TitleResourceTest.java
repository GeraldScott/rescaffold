package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Title;
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
@DisplayName("Title Resource REST API Tests")
class TitleResourceTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    @BeforeEach
    void setUp() {
        // Common request specification for all tests
        requestSpec = new RequestSpecBuilder()
                .setBasePath("/api/titles")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        // Common response specification for successful responses
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }

    // Helper methods for creating test data
    private Title createValidTitle(String code, String description) {
        Title title = new Title();
        title.code = code;
        title.description = description;
        return title;
    }

    private Title createInvalidTitleWithTooLongCode() {
        Title title = new Title();
        title.code = "ABCDEF"; // Invalid: must be 1-5 characters
        title.description = "Invalid Long Code";
        return title;
    }

    private Title createInvalidTitleWithNumericCode() {
        Title title = new Title();
        title.code = "DR1"; // Invalid: must contain only letters
        title.description = "Numeric Code";
        return title;
    }

    // CRUD operation tests

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should create title successfully")
    void testCreateTitle_ValidData() {
        Title title = createValidTitle("MR", "Mister");

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .spec(responseSpec)
                .statusCode(201)
                .body("id", notNullValue())
                .body("code", equalTo("MR"))
                .body("description", equalTo("Mister"))
                .body("isActive", equalTo(true))
                .body("createdBy", equalTo("system"))
                .body("createdAt", notNullValue())
                .body("updatedBy", nullValue())
                .body("updatedAt", nullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/titles/{id} - Should return title by ID")
    void testGetTitleById_ValidId() {
        // First create a title
        Title title = createValidTitle("MS", "Miss");
        Integer createdId = given()
                .spec(requestSpec)
                .body(title)
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
                .body("code", equalTo("MS"))
                .body("description", equalTo("Miss"))
                .body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/titles/code/{code} - Should return title by code")
    void testGetTitleByCode_ValidCode() {
        // First create a title
        Title title = createValidTitle("DR", "Doctor");
        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Then retrieve it by code
        given()
                .spec(requestSpec)
                .when()
                .get("/code/{code}", "DR")
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("code", equalTo("DR"))
                .body("description", equalTo("Doctor"))
                .body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/titles/{id} - Should update title successfully")
    void testUpdateTitle_ValidData() {
        // First create a title
        Title title = createValidTitle("REV", "Reverend");
        Integer createdId = given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then update it
        Title updatedTitle = createValidTitle("PROF", "Professor");
        given()
                .spec(requestSpec)
                .body(updatedTitle)
                .when()
                .put("/{id}", createdId)
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("id", equalTo(createdId))
                .body("code", equalTo("PROF"))
                .body("description", equalTo("Professor"))
                .body("isActive", equalTo(true))
                .body("updatedAt", notNullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/titles/{id} - Should delete title successfully")
    void testDeleteTitle_ValidId() {
        // First create a title
        Title title = createValidTitle("HON", "Honorable");
        Integer createdId = given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then delete it
        given()
                .spec(requestSpec)
                .when()
                .delete("/{id}", createdId)
                .then()
                .statusCode(204);

        // Verify it's deleted
        given()
                .spec(requestSpec)
                .when()
                .get("/{id}", createdId)
                .then()
                .statusCode(404);
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/titles - Should return populated list after creating titles")
    void testGetAllTitles_PopulatedList() {
        // Create multiple titles with unique codes for this test
        given().spec(requestSpec).body(createValidTitle("LORD", "Lord")).when().post().then().statusCode(201);
        given().spec(requestSpec).body(createValidTitle("LADY", "Lady")).when().post().then().statusCode(201);
        given().spec(requestSpec).body(createValidTitle("SIR", "Sir")).when().post().then().statusCode(201);

        // Verify list contains our created titles (size may vary due to test isolation issues)
        given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(3)))
                .body("code", hasItems("LORD", "LADY", "SIR"))
                .body("description", hasItems("Lord", "Lady", "Sir"));
    }

    // Data validation tests

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject null code")
    void testCreateTitle_NullCode() {
        Title title = new Title();
        title.code = null;
        title.description = "No Code";

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject empty code")
    void testCreateTitle_EmptyCode() {
        Title title = new Title();
        title.code = "";
        title.description = "Empty Code";

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject code longer than 5 characters")
    void testCreateTitle_TooLongCode() {
        Title title = createInvalidTitleWithTooLongCode();

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject numeric code")
    void testCreateTitle_NumericCode() {
        Title title = createInvalidTitleWithNumericCode();

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject null description")
    void testCreateTitle_NullDescription() {
        Title title = new Title();
        title.code = "TEST";
        title.description = null;

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject empty description")
    void testCreateTitle_EmptyDescription() {
        Title title = new Title();
        title.code = "TEST";
        title.description = "";

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject blank description")
    void testCreateTitle_BlankDescription() {
        Title title = new Title();
        title.code = "TEST";
        title.description = "   ";

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject when ID is provided")
    void testCreateTitle_WithId() {
        Title title = createValidTitle("ID", "With ID");
        title.id = 999L; // Should not be provided in POST

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("error", containsString("ID must not be included"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject duplicate code")
    void testCreateTitle_DuplicateCode() {
        // Create first title with unique code for this test
        Title first = createValidTitle("DUP", "Duplicate Code Test First");
        given()
                .spec(requestSpec)
                .body(first)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Try to create second with same code
        Title duplicate = createValidTitle("DUP", "Duplicate Code Test Second");
        given()
                .spec(requestSpec)
                .body(duplicate)
                .when()
                .post()
                .then()
                .statusCode(409)
                .body("error", containsString("already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should reject duplicate description")
    void testCreateTitle_DuplicateDescription() {
        // Create first title with unique data for this test
        Title first = createValidTitle("FIRST", "Duplicate Desc Test Unique");
        given()
                .spec(requestSpec)
                .body(first)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Try to create second with same description
        Title duplicate = createValidTitle("SECND", "Duplicate Desc Test Unique");
        given()
                .spec(requestSpec)
                .body(duplicate)
                .when()
                .post()
                .then()
                .statusCode(409)
                .body("error", containsString("already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/titles - Should properly process valid uppercase code")
    void testCreateTitle_ValidUppercaseCode() {
        Title title = createValidTitle("UPPER", "Uppercase");

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .post()
                .then()
                .spec(responseSpec)
                .statusCode(201)
                .body("code", equalTo("UPPER"))
                .body("description", equalTo("Uppercase"));
    }

    // Error handling tests

    @Test
    @TestTransaction
    @DisplayName("GET /api/titles/{id} - Should return 404 for non-existent ID")
    void testGetTitleById_NotFound() {
        given()
                .spec(requestSpec)
                .when()
                .get("/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", containsString("Entity not found"));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/titles/code/{code} - Should return 404 for non-existent code")
    void testGetTitleByCode_NotFound() {
        given()
                .spec(requestSpec)
                .when()
                .get("/code/{code}", "XXXXX")
                .then()
                .statusCode(404)
                .body("error", containsString("Entity not found"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/titles/{id} - Should return 404 for non-existent ID")
    void testUpdateTitle_NotFound() {
        Title title = createValidTitle("UPDT", "Update Non-Existent");

        given()
                .spec(requestSpec)
                .body(title)
                .when()
                .put("/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", containsString("Entity not found"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/titles/{id} - Should return 409 for duplicate code")
    void testUpdateTitle_DuplicateCode() {
        // Create two titles with unique codes for this test
        Title first = createValidTitle("UPCA", "Update Code Test First");
        Integer firstId = given().spec(requestSpec).body(first).when().post().then().statusCode(201).extract().path("id");

        Title second = createValidTitle("UPCB", "Update Code Test Second");
        given().spec(requestSpec).body(second).when().post().then().statusCode(201);

        // Try to update first with second's code
        Title update = createValidTitle("UPCB", "Updated First");
        given()
                .spec(requestSpec)
                .body(update)
                .when()
                .put("/{id}", firstId)
                .then()
                .statusCode(409)
                .body("error", containsString("already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/titles/{id} - Should return 409 for duplicate description")
    void testUpdateTitle_DuplicateDescription() {
        // Create two titles with unique data for this test
        Title first = createValidTitle("UPDA", "Update Desc Test First");
        Integer firstId = given().spec(requestSpec).body(first).when().post().then().statusCode(201).extract().path("id");

        Title second = createValidTitle("UPDB", "Update Desc Test Second");
        given().spec(requestSpec).body(second).when().post().then().statusCode(201);

        // Try to update first with second's description
        Title update = createValidTitle("UPDA", "Update Desc Test Second");
        given()
                .spec(requestSpec)
                .body(update)
                .when()
                .put("/{id}", firstId)
                .then()
                .statusCode(409)
                .body("error", containsString("already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/titles/{id} - Should return 404 for non-existent ID")
    void testDeleteTitle_NotFound() {
        given()
                .spec(requestSpec)
                .when()
                .delete("/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", containsString("Entity not found"));
    }
}