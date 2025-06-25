package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Gender;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Gender Resource API Tests")
class GenderResourceTest {

    @BeforeAll
    @TestTransaction
    void setupDatabase() {
        // Clean up existing records before running tests
        Gender.deleteAll();
    }

    @BeforeEach
    @TestTransaction
    void cleanupBeforeTest() {
        // Clean up existing records before each test
        Gender.deleteAll();
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders - should return initial data")
    void shouldReturnInitialData() {
        given()
                .when()
                .get("/api/genders")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(4))
                .body("findAll { it.code == 'F' }.size()", equalTo(1))
                .body("findAll { it.code == 'M' }.size()", equalTo(1))
                .body("findAll { it.code == 'O' }.size()", equalTo(1))
                .body("findAll { it.code == 'U' }.size()", equalTo(1));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should create gender successfully")
    void shouldCreateGenderSuccessfully() {
        Map<String, Object> genderData = Map.of(
                "code", "T",
                "description", "Test Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("code", equalTo("T"))
                .body("description", equalTo("Test Gender"))
                .body("isActive", equalTo(true))
                .body("createdBy", equalTo("system"))
                .body("createdAt", notNullValue())
                .body("id", notNullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject gender with null code")
    void shouldRejectGenderWithNullCode() {
        Map<String, Object> genderData = Map.of(
                "description", "Test Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject gender with empty code")
    void shouldRejectGenderWithEmptyCode() {
        Map<String, Object> genderData = Map.of(
                "code", "",
                "description", "Test Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject gender with multi-character code")
    void shouldRejectGenderWithMultiCharacterCode() {
        Map<String, Object> genderData = Map.of(
                "code", "AB",
                "description", "Test Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject gender with non-alphabetic code")
    void shouldRejectGenderWithNonAlphabeticCode() {
        Map<String, Object> genderData = Map.of(
                "code", "1",
                "description", "Test Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject gender with lowercase code")
    void shouldRejectGenderWithLowercaseCode() {
        Map<String, Object> genderData = Map.of(
                "code", "t",
                "description", "Test Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject gender with null description")
    void shouldRejectGenderWithNullDescription() {
        Map<String, Object> genderData = Map.of(
                "code", "T"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("Description cannot be blank"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject gender with empty description")
    void shouldRejectGenderWithEmptyDescription() {
        Map<String, Object> genderData = Map.of(
                "code", "T",
                "description", ""
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("Gender description is required"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject duplicate code")
    void shouldRejectDuplicateCode() {
        // Create first gender
        Map<String, Object> firstGender = Map.of(
                "code", "X",
                "description", "Test Duplicate Code Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(firstGender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201);

        // Try to create second gender with same code
        Map<String, Object> duplicateGender = Map.of(
                "code", "X",
                "description", "Another Test Duplicate Code Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(duplicateGender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(409)
                .contentType(ContentType.JSON)
                .body("error", containsString("Gender with code 'X' already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject duplicate description")
    void shouldRejectDuplicateDescription() {
        // Create first gender
        Map<String, Object> firstGender = Map.of(
                "code", "Y",
                "description", "Test Duplicate Description Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(firstGender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201);

        // Try to create second gender with same description
        Map<String, Object> duplicateGender = Map.of(
                "code", "Z",
                "description", "Test Duplicate Description Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(duplicateGender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(409)
                .contentType(ContentType.JSON)
                .body("error", containsString("Gender with description 'Test Duplicate Description Gender' already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should normalize code to uppercase")
    void shouldNormalizeCodeToUppercase() {
        // Note: Based on GenderService, lowercase codes should be rejected by validation
        // But if they pass validation, they would be normalized to uppercase
        // This test verifies the validation rejects lowercase as expected
        Map<String, Object> genderData = Map.of(
                "code", "a",
                "description", "Test Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/{id} - should return gender by ID")
    void shouldReturnGenderById() {
        // Create a gender first
        Map<String, Object> genderData = Map.of(
                "code", "G",
                "description", "Get Test Gender"
        );

        var createResponse = given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201)
                .extract().response();

        Long genderId = createResponse.jsonPath().getLong("id");

        // Get the gender by ID
        given()
                .when()
                .get("/api/genders/{id}", genderId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(genderId.intValue()))
                .body("code", equalTo("G"))
                .body("description", equalTo("Get Test Gender"))
                .body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/{id} - should return 404 for non-existent ID")
    void shouldReturn404ForNonExistentId() {
        given()
                .when()
                .get("/api/genders/{id}", 99999L)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", containsString("Entity not found with id: 99999"));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/code/{code} - should return gender by code")
    void shouldReturnGenderByCode() {
        // Create a gender first
        Map<String, Object> genderData = Map.of(
                "code", "C",
                "description", "Code Test Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201);

        // Get the gender by code
        given()
                .when()
                .get("/api/genders/code/{code}", "C")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo("C"))
                .body("description", equalTo("Code Test Gender"))
                .body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/code/{code} - should return 404 for non-existent code")
    void shouldReturn404ForNonExistentCode() {
        given()
                .when()
                .get("/api/genders/code/{code}", "Q")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", containsString("Entity not found with code: Q"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - should update gender successfully")
    void shouldUpdateGenderSuccessfully() {
        // Create a gender first
        Map<String, Object> genderData = Map.of(
                "code", "U",
                "description", "Update Test Gender"
        );

        var createResponse = given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201)
                .extract().response();

        Long genderId = createResponse.jsonPath().getLong("id");

        // Update the gender
        Map<String, Object> updateData = Map.of(
                "code", "V",
                "description", "Updated Gender",
                "isActive", false
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/api/genders/{id}", genderId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(genderId.intValue()))
                .body("code", equalTo("V"))
                .body("description", equalTo("Updated Gender"))
                .body("isActive", equalTo(false))
                .body("updatedAt", notNullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - should return 404 for non-existent ID")
    void shouldReturn404ForUpdateNonExistentId() {
        Map<String, Object> updateData = Map.of(
                "code", "N",
                "description", "Non-existent Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/api/genders/{id}", 99999L)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", containsString("Entity not found with id: 99999"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - should reject duplicate code in update")
    void shouldRejectDuplicateCodeInUpdate() {
        // Create two genders
        Map<String, Object> firstGender = Map.of(
                "code", "D",
                "description", "First Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(firstGender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201);

        Map<String, Object> secondGender = Map.of(
                "code", "E",
                "description", "Second Gender"
        );

        var createResponse = given()
                .contentType(ContentType.JSON)
                .body(secondGender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201)
                .extract().response();

        Long secondGenderId = createResponse.jsonPath().getLong("id");

        // Try to update second gender with first gender's code
        Map<String, Object> updateData = Map.of(
                "code", "D",
                "description", "Updated Second Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/api/genders/{id}", secondGenderId)
                .then()
                .statusCode(409)
                .contentType(ContentType.JSON)
                .body("error", containsString("Another gender with code 'D' already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - should allow partial updates")
    void shouldAllowPartialUpdates() {
        // Create a gender first
        Map<String, Object> genderData = Map.of(
                "code", "P",
                "description", "Partial Update Gender"
        );

        var createResponse = given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201)
                .extract().response();

        Long genderId = createResponse.jsonPath().getLong("id");

        // Update only the description
        Map<String, Object> updateData = Map.of(
                "description", "Partially Updated Gender"
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/api/genders/{id}", genderId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo("P")) // Should remain unchanged
                .body("description", equalTo("Partially Updated Gender"));
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/genders/{id} - should delete gender successfully")
    void shouldDeleteGenderSuccessfully() {
        // Create a gender first
        Map<String, Object> genderData = Map.of(
                "code", "R",
                "description", "Delete Test Gender"
        );

        var createResponse = given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201)
                .extract().response();

        Long genderId = createResponse.jsonPath().getLong("id");

        // Delete the gender
        given()
                .when()
                .delete("/api/genders/{id}", genderId)
                .then()
                .statusCode(204);

        // Verify gender is deleted
        given()
                .when()
                .get("/api/genders/{id}", genderId)
                .then()
                .statusCode(404);
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/genders/{id} - should return 404 for non-existent ID")
    void shouldReturn404ForDeleteNonExistentId() {
        given()
                .when()
                .delete("/api/genders/{id}", 99999L)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", containsString("Entity not found with id: 99999"));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders - should return all created genders")
    void shouldReturnAllCreatedGenders() {
        // Create multiple genders
        Map<String, Object> gender1 = Map.of("code", "A", "description", "Alpha");
        Map<String, Object> gender2 = Map.of("code", "B", "description", "Beta");

        given().contentType(ContentType.JSON).body(gender1).post("/api/genders");
        given().contentType(ContentType.JSON).body(gender2).post("/api/genders");

        // Get all genders
        given()
                .when()
                .get("/api/genders")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(2))
                .body("findAll { it.code == 'A' }.size()", equalTo(1))
                .body("findAll { it.code == 'B' }.size()", equalTo(1));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should reject gender with ID in payload")
    void shouldRejectGenderWithIdInPayload() {
        Map<String, Object> genderData = Map.of(
                "id", 123L,
                "code", "I",
                "description", "Invalid Gender with ID"
        );

        given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("ID must not be included in POST request"));
    }
}
