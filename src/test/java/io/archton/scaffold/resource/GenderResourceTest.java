package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Gender;
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
@DisplayName("Gender Resource REST API Tests")
class GenderResourceTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    @BeforeEach
    void setUp() {
        // Common request specification for all tests
        requestSpec = new RequestSpecBuilder().setBasePath("/api/genders").setContentType(ContentType.JSON).setAccept(ContentType.JSON).build();

        // Common response specification for successful responses
        responseSpec = new ResponseSpecBuilder().expectContentType(ContentType.JSON).build();
    }

    // Helper methods for creating test data
    private Gender createValidGender(String code, String description) {
        Gender gender = new Gender();
        gender.code = code;
        gender.description = description;
        return gender;
    }

    private Gender createInvalidGenderWithMultiCharCode() {
        Gender gender = new Gender();
        gender.code = "AB"; // Invalid: must be single character
        gender.description = "Invalid Multi-Char";
        return gender;
    }

    private Gender createInvalidGenderWithNumericCode() {
        Gender gender = new Gender();
        gender.code = "1"; // Invalid: must be alphabetic
        gender.description = "Numeric Code";
        return gender;
    }

    // CRUD operation tests

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should create gender successfully")
    void testCreateGender_ValidData() {
        Gender gender = createValidGender("M", "Male");

        given().spec(requestSpec).body(gender).when().post().then().spec(responseSpec).statusCode(201).body("id", notNullValue()).body("code", equalTo("M")).body("description", equalTo("Male")).body("isActive", equalTo(true)).body("createdBy", equalTo("system")).body("createdAt", notNullValue()).body("updatedBy", nullValue()).body("updatedAt", nullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/{id} - Should return gender by ID")
    void testGetGenderById_ValidId() {
        // First create a gender
        Gender gender = createValidGender("F", "Female");
        Integer createdId = given().spec(requestSpec).body(gender).when().post().then().statusCode(201).extract().path("id");

        // Then retrieve it by ID
        given().spec(requestSpec).when().get("/{id}", createdId).then().spec(responseSpec).statusCode(200).body("id", equalTo(createdId)).body("code", equalTo("F")).body("description", equalTo("Female")).body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/code/{code} - Should return gender by code")
    void testGetGenderByCode_ValidCode() {
        // First create a gender
        Gender gender = createValidGender("O", "Other");
        given().spec(requestSpec).body(gender).when().post().then().statusCode(201);

        // Then retrieve it by code
        given().spec(requestSpec).when().get("/code/{code}", "O").then().spec(responseSpec).statusCode(200).body("code", equalTo("O")).body("description", equalTo("Other")).body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - Should update gender successfully")
    void testUpdateGender_ValidData() {
        // First create a gender
        Gender gender = createValidGender("X", "Original");
        Integer createdId = given().spec(requestSpec).body(gender).when().post().then().statusCode(201).extract().path("id");

        // Then update it
        Gender updatedGender = createValidGender("Y", "Updated");
        given().spec(requestSpec).body(updatedGender).when().put("/{id}", createdId).then().spec(responseSpec).statusCode(200).body("id", equalTo(createdId)).body("code", equalTo("Y")).body("description", equalTo("Updated")).body("isActive", equalTo(true)).body("updatedAt", notNullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/genders/{id} - Should delete gender successfully")
    void testDeleteGender_ValidId() {
        // First create a gender
        Gender gender = createValidGender("Z", "To Delete");
        Integer createdId = given().spec(requestSpec).body(gender).when().post().then().statusCode(201).extract().path("id");

        // Then delete it
        given().spec(requestSpec).when().delete("/{id}", createdId).then().statusCode(204);

        // Verify it's deleted
        given().spec(requestSpec).when().get("/{id}", createdId).then().statusCode(404);
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders - Should return populated list after creating genders")
    void testGetAllGenders_PopulatedList() {
        // Create multiple genders with unique codes for this test
        given().spec(requestSpec).body(createValidGender("J", "List Test Alpha")).when().post().then().statusCode(201);
        given().spec(requestSpec).body(createValidGender("K", "List Test Beta")).when().post().then().statusCode(201);
        given().spec(requestSpec).body(createValidGender("L", "List Test Charlie")).when().post().then().statusCode(201);

        // Verify list contains our created genders (size may vary due to test isolation issues)
        given().spec(requestSpec).when().get().then().spec(responseSpec).statusCode(200).body("$", hasSize(greaterThanOrEqualTo(3))).body("code", hasItems("J", "K", "L")).body("description", hasItems("List Test Alpha", "List Test Beta", "List Test Charlie"));
    }

    // Data validation tests

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject null code")
    void testCreateGender_NullCode() {
        Gender gender = new Gender();
        gender.code = null;
        gender.description = "No Code";

        given().spec(requestSpec).body(gender).when().post().then().statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject empty code")
    void testCreateGender_EmptyCode() {
        Gender gender = new Gender();
        gender.code = "";
        gender.description = "Empty Code";

        given().spec(requestSpec).body(gender).when().post().then().statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject multi-character code")
    void testCreateGender_MultiCharCode() {
        Gender gender = createInvalidGenderWithMultiCharCode();

        given().spec(requestSpec).body(gender).when().post().then().statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject numeric code")
    void testCreateGender_NumericCode() {
        Gender gender = createInvalidGenderWithNumericCode();

        given().spec(requestSpec).body(gender).when().post().then().statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject null description")
    void testCreateGender_NullDescription() {
        Gender gender = new Gender();
        gender.code = "T";
        gender.description = null;

        given().spec(requestSpec).body(gender).when().post().then().statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject empty description")
    void testCreateGender_EmptyDescription() {
        Gender gender = new Gender();
        gender.code = "T";
        gender.description = "";

        given().spec(requestSpec).body(gender).when().post().then().statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject blank description")
    void testCreateGender_BlankDescription() {
        Gender gender = new Gender();
        gender.code = "T";
        gender.description = "   ";

        given().spec(requestSpec).body(gender).when().post().then().statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject when ID is provided")
    void testCreateGender_WithId() {
        Gender gender = createValidGender("I", "With ID");
        gender.id = 999L; // Should not be provided in POST

        given().spec(requestSpec).body(gender).when().post().then().statusCode(400).body("error", containsString("ID must not be included"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject duplicate code")
    void testCreateGender_DuplicateCode() {
        // Create first gender with unique code for this test
        Gender first = createValidGender("Q", "Duplicate Code Test First");
        given().spec(requestSpec).body(first).when().post().then().statusCode(201);

        // Try to create second with same code
        Gender duplicate = createValidGender("Q", "Duplicate Code Test Second");
        given().spec(requestSpec).body(duplicate).when().post().then().statusCode(409).body("error", containsString("already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should reject duplicate description")
    void testCreateGender_DuplicateDescription() {
        // Create first gender with unique data for this test
        Gender first = createValidGender("T", "Duplicate Desc Test Unique");
        given().spec(requestSpec).body(first).when().post().then().statusCode(201);

        // Try to create second with same description
        Gender duplicate = createValidGender("Y", "Duplicate Desc Test Unique");
        given().spec(requestSpec).body(duplicate).when().post().then().statusCode(409).body("error", containsString("already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - Should properly process valid uppercase code")
    void testCreateGender_ValidUppercaseCode() {
        Gender gender = createValidGender("U", "Uppercase");

        given().spec(requestSpec).body(gender).when().post().then().spec(responseSpec).statusCode(201).body("code", equalTo("U")).body("description", equalTo("Uppercase"));
    }

    // Error handling tests

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/{id} - Should return 404 for non-existent ID")
    void testGetGenderById_NotFound() {
        given().spec(requestSpec).when().get("/{id}", 99999L).then().statusCode(404).body("error", containsString("Entity not found"));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/code/{code} - Should return 404 for non-existent code")
    void testGetGenderByCode_NotFound() {
        given().spec(requestSpec).when().get("/code/{code}", "X").then().statusCode(404).body("error", containsString("Entity not found"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - Should return 404 for non-existent ID")
    void testUpdateGender_NotFound() {
        Gender gender = createValidGender("U", "Update Non-Existent");

        given().spec(requestSpec).body(gender).when().put("/{id}", 99999L).then().statusCode(404).body("error", containsString("Entity not found"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - Should return 409 for duplicate code")
    void testUpdateGender_DuplicateCode() {
        // Create two genders with unique codes for this test
        Gender first = createValidGender("R", "Update Test First");
        Integer firstId = given().spec(requestSpec).body(first).when().post().then().statusCode(201).extract().path("id");

        Gender second = createValidGender("S", "Update Test Second");
        Integer secondId = given().spec(requestSpec).body(second).when().post().then().statusCode(201).extract().path("id");

        // Try to update first with second's code
        Gender update = createValidGender("S", "Updated First");
        given().spec(requestSpec).body(update).when().put("/{id}", firstId).then().statusCode(409).body("error", containsString("already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - Should return 409 for duplicate description")
    void testUpdateGender_DuplicateDescription() {
        // Create two genders with unique data for this test
        Gender first = createValidGender("V", "Update Desc Test First");
        Integer firstId = given().spec(requestSpec).body(first).when().post().then().statusCode(201).extract().path("id");

        Gender second = createValidGender("W", "Update Desc Test Second");
        Integer secondId = given().spec(requestSpec).body(second).when().post().then().statusCode(201).extract().path("id");

        // Try to update first with second's description
        Gender update = createValidGender("V", "Update Desc Test Second");
        given().spec(requestSpec).body(update).when().put("/{id}", firstId).then().statusCode(409).body("error", containsString("already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/genders/{id} - Should return 404 for non-existent ID")
    void testDeleteGender_NotFound() {
        given().spec(requestSpec).when().delete("/{id}", 99999L).then().statusCode(404).body("error", containsString("Entity not found"));
    }
}