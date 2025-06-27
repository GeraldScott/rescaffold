package io.archton.scaffold.resource;

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
@DisplayName("IdType Resource REST API Tests")
class IdTypeResourceTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    @BeforeEach
    void setUp() {
        // Common request specification for all tests
        requestSpec = new RequestSpecBuilder()
                .setBasePath("/api/id-types")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        // Common response specification for successful responses
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }

    // Helper methods for creating test data
    private IdType createValidIdType(String code, String description) {
        IdType idType = new IdType();
        idType.code = code;
        idType.description = description;
        return idType;
    }

    private IdType createInvalidIdTypeWithTooLongCode() {
        IdType idType = new IdType();
        idType.code = "ABCDEF"; // Invalid: must be 1-5 characters
        idType.description = "Invalid Long Code";
        return idType;
    }

    private IdType createInvalidIdTypeWithNumericCode() {
        IdType idType = new IdType();
        idType.code = "ID1"; // Invalid: must contain only letters
        idType.description = "Numeric Code";
        return idType;
    }

    // CRUD operation tests

    @Test
    @TestTransaction
    @DisplayName("GET /api/id-types - Should return empty list initially")
    void testGetAllIdTypes_EmptyList() {
        given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should create id type successfully")
    void testCreateIdType_ValidData() {
        IdType idType = createValidIdType("DL", "Driver's License");

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .spec(responseSpec)
                .statusCode(201)
                .body("id", notNullValue())
                .body("code", equalTo("DL"))
                .body("description", equalTo("Driver's License"))
                .body("isActive", equalTo(true))
                .body("createdBy", equalTo("system"))
                .body("createdAt", notNullValue())
                .body("updatedBy", nullValue())
                .body("updatedAt", nullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/id-types/{id} - Should return id type by ID")
    void testGetIdTypeById_ValidId() {
        // First create an id type
        IdType idType = createValidIdType("SSN", "Social Security Number");
        Integer createdId = given()
                .spec(requestSpec)
                .body(idType)
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
                .body("code", equalTo("SSN"))
                .body("description", equalTo("Social Security Number"))
                .body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/id-types/code/{code} - Should return id type by code")
    void testGetIdTypeByCode_ValidCode() {
        // First create an id type
        IdType idType = createValidIdType("VIN", "Vehicle Identification Number");
        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Then retrieve it by code
        given()
                .spec(requestSpec)
                .when()
                .get("/code/{code}", "VIN")
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("code", equalTo("VIN"))
                .body("description", equalTo("Vehicle Identification Number"))
                .body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/id-types/{id} - Should update id type successfully")
    void testUpdateIdType_ValidData() {
        // First create an id type
        IdType idType = createValidIdType("TIN", "Tax Identification Number");
        Integer createdId = given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then update it
        IdType updatedIdType = createValidIdType("EIN", "Employer Identification Number");
        given()
                .spec(requestSpec)
                .body(updatedIdType)
                .when()
                .put("/{id}", createdId)
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("id", equalTo(createdId))
                .body("code", equalTo("EIN"))
                .body("description", equalTo("Employer Identification Number"))
                .body("isActive", equalTo(true))
                .body("updatedAt", notNullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/id-types/{id} - Should delete id type successfully")
    void testDeleteIdType_ValidId() {
        // First create an id type
        IdType idType = createValidIdType("ITIN", "Individual Taxpayer Identification Number");
        Integer createdId = given()
                .spec(requestSpec)
                .body(idType)
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
    @DisplayName("GET /api/id-types - Should return populated list after creating id types")
    void testGetAllIdTypes_PopulatedList() {
        // Create multiple id types with unique codes for this test
        given().spec(requestSpec).body(createValidIdType("BC", "Birth Certificate")).when().post().then().statusCode(201);
        given().spec(requestSpec).body(createValidIdType("MC", "Marriage Certificate")).when().post().then().statusCode(201);
        given().spec(requestSpec).body(createValidIdType("DC", "Death Certificate")).when().post().then().statusCode(201);

        // Verify list contains our created id types (size may vary due to test isolation issues)
        given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(3)))
                .body("code", hasItems("BC", "MC", "DC"))
                .body("description", hasItems("Birth Certificate", "Marriage Certificate", "Death Certificate"));
    }

    // Data validation tests

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should reject null code")
    void testCreateIdType_NullCode() {
        IdType idType = new IdType();
        idType.code = null;
        idType.description = "No Code";

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should reject empty code")
    void testCreateIdType_EmptyCode() {
        IdType idType = new IdType();
        idType.code = "";
        idType.description = "Empty Code";

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should reject code longer than 5 characters")
    void testCreateIdType_TooLongCode() {
        IdType idType = createInvalidIdTypeWithTooLongCode();

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should reject numeric code")
    void testCreateIdType_NumericCode() {
        IdType idType = createInvalidIdTypeWithNumericCode();

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should reject null description")
    void testCreateIdType_NullDescription() {
        IdType idType = new IdType();
        idType.code = "TEST";
        idType.description = null;

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should reject empty description")
    void testCreateIdType_EmptyDescription() {
        IdType idType = new IdType();
        idType.code = "TEST";
        idType.description = "";

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should reject blank description")
    void testCreateIdType_BlankDescription() {
        IdType idType = new IdType();
        idType.code = "TEST";
        idType.description = "   ";

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should reject when ID is provided")
    void testCreateIdType_WithId() {
        IdType idType = createValidIdType("WID", "With ID");
        idType.id = 999L; // Should not be provided in POST

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("error", containsString("ID must not be included"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/id-types - Should reject duplicate code")
    void testCreateIdType_DuplicateCode() {
        // Create first id type with unique code for this test
        IdType first = createValidIdType("DUPE", "Duplicate Code Test First");
        given()
                .spec(requestSpec)
                .body(first)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Try to create second with same code
        IdType duplicate = createValidIdType("DUPE", "Duplicate Code Test Second");
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
    @DisplayName("POST /api/id-types - Should reject duplicate description")
    void testCreateIdType_DuplicateDescription() {
        // Create first id type with unique data for this test
        IdType first = createValidIdType("FIRST", "Duplicate Desc Test Unique");
        given()
                .spec(requestSpec)
                .body(first)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Try to create second with same description
        IdType duplicate = createValidIdType("SECND", "Duplicate Desc Test Unique");
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
    @DisplayName("POST /api/id-types - Should properly process valid uppercase code")
    void testCreateIdType_ValidUppercaseCode() {
        IdType idType = createValidIdType("UPPER", "Uppercase");

        given()
                .spec(requestSpec)
                .body(idType)
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
    @DisplayName("GET /api/id-types/{id} - Should return 404 for non-existent ID")
    void testGetIdTypeById_NotFound() {
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
    @DisplayName("GET /api/id-types/code/{code} - Should return 404 for non-existent code")
    void testGetIdTypeByCode_NotFound() {
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
    @DisplayName("PUT /api/id-types/{id} - Should return 404 for non-existent ID")
    void testUpdateIdType_NotFound() {
        IdType idType = createValidIdType("UPDT", "Update Non-Existent");

        given()
                .spec(requestSpec)
                .body(idType)
                .when()
                .put("/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", containsString("Entity not found"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/id-types/{id} - Should return 409 for duplicate code")
    void testUpdateIdType_DuplicateCode() {
        // Create two id types with unique codes for this test
        IdType first = createValidIdType("UPC1", "Update Code Test First");
        Integer firstId = given().spec(requestSpec).body(first).when().post().then().statusCode(201).extract().path("id");

        IdType second = createValidIdType("UPC2", "Update Code Test Second");
        given().spec(requestSpec).body(second).when().post().then().statusCode(201);

        // Try to update first with second's code
        IdType update = createValidIdType("UPC2", "Updated First");
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
    @DisplayName("PUT /api/id-types/{id} - Should return 409 for duplicate description")
    void testUpdateIdType_DuplicateDescription() {
        // Create two id types with unique data for this test
        IdType first = createValidIdType("UPD1", "Update Desc Test First");
        Integer firstId = given().spec(requestSpec).body(first).when().post().then().statusCode(201).extract().path("id");

        IdType second = createValidIdType("UPD2", "Update Desc Test Second");
        given().spec(requestSpec).body(second).when().post().then().statusCode(201);

        // Try to update first with second's description
        IdType update = createValidIdType("UPD1", "Update Desc Test Second");
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
    @DisplayName("DELETE /api/id-types/{id} - Should return 404 for non-existent ID")
    void testDeleteIdType_NotFound() {
        given()
                .spec(requestSpec)
                .when()
                .delete("/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", containsString("Entity not found"));
    }
}