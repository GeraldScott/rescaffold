package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Gender;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenderResourceTest {

    private static UUID testGenderId;
    private static final String TEST_CODE = "M";
    private static final String TEST_DESCRIPTION = "Male";
    private static final String ALTERNATE_CODE = "F";
    private static final String ALTERNATE_DESCRIPTION = "Female";

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean existing test data
        Gender.delete("code in (?1, ?2)", TEST_CODE, ALTERNATE_CODE);
    }

    @Test
    @Order(1)
    void testCreateGender_Success() {
        Gender gender = new Gender(TEST_CODE, TEST_DESCRIPTION);

        String createdId = given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201)
                .body("code", equalTo(TEST_CODE))
                .body("description", equalTo(TEST_DESCRIPTION))
                .body("isActive", equalTo(true))
                .body("id", notNullValue())
                .body("createdAt", notNullValue())
                .body("createdBy", equalTo("system"))
                .extract()
                .path("id");

        testGenderId = UUID.fromString(createdId);
    }

    @Test
    @Order(2)
    void testGetAllGenders_Success() {
        // Create test data
        createTestGender(TEST_CODE, TEST_DESCRIPTION);

        given()
                .when()
                .get("/api/genders")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("find { it.code == '" + TEST_CODE + "' }.description", equalTo(TEST_DESCRIPTION));
    }

    @Test
    @Order(3)
    void testGetGenderById_Success() {
        UUID genderId = createTestGender(TEST_CODE, TEST_DESCRIPTION);

        given()
                .when()
                .get("/api/genders/" + genderId)
                .then()
                .statusCode(200)
                .body("id", equalTo(genderId.toString()))
                .body("code", equalTo(TEST_CODE))
                .body("description", equalTo(TEST_DESCRIPTION));
    }

    @Test
    @Order(4)
    void testGetGenderByCode_Success() {
        createTestGender(TEST_CODE, TEST_DESCRIPTION);

        given()
                .when()
                .get("/api/genders/code/" + TEST_CODE)
                .then()
                .statusCode(200)
                .body("code", equalTo(TEST_CODE))
                .body("description", equalTo(TEST_DESCRIPTION));
    }

    @Test
    @Order(5)
    void testUpdateGender_Success() {
        UUID genderId = createTestGender(TEST_CODE, TEST_DESCRIPTION);
        Gender updateGender = new Gender(ALTERNATE_CODE, ALTERNATE_DESCRIPTION);

        given()
                .contentType(ContentType.JSON)
                .body(updateGender)
                .when()
                .put("/api/genders/" + genderId)
                .then()
                .statusCode(200)
                .body("id", equalTo(genderId.toString()))
                .body("code", equalTo(ALTERNATE_CODE))
                .body("description", equalTo(ALTERNATE_DESCRIPTION))
                .body("updatedAt", notNullValue());
    }

    @Test
    @Order(6)
    void testDeleteGender_Success() {
        UUID genderId = createTestGender(TEST_CODE, TEST_DESCRIPTION);

        given()
                .when()
                .delete("/api/genders/" + genderId)
                .then()
                .statusCode(204);

        // Verify deletion
        given()
                .when()
                .get("/api/genders/" + genderId)
                .then()
                .statusCode(404);
    }

    // Validation Tests
    @Test
    void testCreateGender_WithId_BadRequest() {
        Gender gender = new Gender(TEST_CODE, TEST_DESCRIPTION);
        gender.id = UUID.randomUUID();

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400)
                .body("error", containsString("ID must not be included"));
    }

    @Test
    void testCreateGender_InvalidCode_BadRequest() {
        Gender gender = new Gender("invalid", TEST_DESCRIPTION);

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    void testCreateGender_BlankCode_BadRequest() {
        Gender gender = new Gender("", TEST_DESCRIPTION);

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    void testCreateGender_BlankDescription_BadRequest() {
        Gender gender = new Gender(TEST_CODE, "");

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400);
    }

    // Conflict Tests
    @Test
    void testCreateGender_DuplicateCode_Conflict() {
        createTestGender(TEST_CODE, TEST_DESCRIPTION);
        Gender duplicateGender = new Gender(TEST_CODE, "Different Description");

        given()
                .contentType(ContentType.JSON)
                .body(duplicateGender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(409)
                .body("error", containsString("already exists"));
    }

    @Test
    void testCreateGender_DuplicateDescription_Conflict() {
        createTestGender(TEST_CODE, TEST_DESCRIPTION);
        Gender duplicateGender = new Gender("X", TEST_DESCRIPTION);

        given()
                .contentType(ContentType.JSON)
                .body(duplicateGender)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(409)
                .body("error", containsString("already exists"));
    }

    @Test
    void testUpdateGender_DuplicateCode_Conflict() {
        UUID genderId1 = createTestGender(TEST_CODE, TEST_DESCRIPTION);
        createTestGender(ALTERNATE_CODE, ALTERNATE_DESCRIPTION);

        Gender updateGender = new Gender(ALTERNATE_CODE, "New Description");

        given()
                .contentType(ContentType.JSON)
                .body(updateGender)
                .when()
                .put("/api/genders/" + genderId1)
                .then()
                .statusCode(409)
                .body("error", containsString("already exists"));
    }

    // Not Found Tests
    @Test
    void testGetGenderById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        given()
                .when()
                .get("/api/genders/" + nonExistentId)
                .then()
                .statusCode(404)
                .body("error", containsString("not found"));
    }

    @Test
    void testGetGenderByCode_NotFound() {
        given()
                .when()
                .get("/api/genders/code/Z")
                .then()
                .statusCode(404)
                .body("error", containsString("not found"));
    }

    @Test
    void testUpdateGender_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        Gender updateGender = new Gender(TEST_CODE, TEST_DESCRIPTION);

        given()
                .contentType(ContentType.JSON)
                .body(updateGender)
                .when()
                .put("/api/genders/" + nonExistentId)
                .then()
                .statusCode(404)
                .body("error", containsString("not found"));
    }

    @Test
    void testDeleteGender_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        given()
                .when()
                .delete("/api/genders/" + nonExistentId)
                .then()
                .statusCode(404)
                .body("error", containsString("not found"));
    }

    // Error Handling Tests
    @Test
    void testGetGenderById_InvalidUUID() {
        given()
                .when()
                .get("/api/genders/invalid-uuid")
                .then()
                .statusCode(404); // JAX-RS returns 404 for invalid path params
    }

    // Helper Methods
    @Transactional
    public UUID createTestGender(String code, String description) {
        Gender gender = new Gender(code, description);
        gender.persist();
        return gender.id;
    }
}