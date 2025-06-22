package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Gender;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Assumptions;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenderResourceTest {

    private static Long createdGenderId;
    private static final String VALID_CODE = "T";
    private static final String VALID_DESCRIPTION = "Test Gender";


    @Test
    @Order(1)
    @DisplayName("GET /api/genders - Should return list of genders")
    void testGetAllGenders() {
        given()
                .when().get("/api/genders")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThan(-1))); // Accept empty list or list with items
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/genders - Should create gender successfully")
    void testCreateGender() {
        Gender gender = new Gender();
        gender.code = VALID_CODE;
        gender.description = VALID_DESCRIPTION;

        Integer idAsInteger = given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("code", equalTo(VALID_CODE))
                .body("description", equalTo(VALID_DESCRIPTION))
                .body("id", notNullValue())
                .body("isActive", equalTo(true))
                .body("createdBy", equalTo("system"))
                .body("createdAt", notNullValue())
                .extract().path("id");
        
        createdGenderId = idAsInteger.longValue();
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/genders - Should reject gender with ID included")
    void testCreateGenderWithId() {
        Gender gender = new Gender();
        gender.id = 999L;
        gender.code = "X";
        gender.description = "Test Gender";

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(400)
                .body("error", equalTo("ID must not be included in POST request"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/genders - Should reject duplicate code")
    void testCreateGenderDuplicateCode() {
        // First create a gender
        Gender firstGender = new Gender();
        firstGender.code = "A";
        firstGender.description = "First Gender";

        given()
                .contentType(ContentType.JSON)
                .body(firstGender)
                .when().post("/api/genders")
                .then()
                .statusCode(201);

        // Try to create another with same code
        Gender gender = new Gender();
        gender.code = "A"; // Same as already created
        gender.description = "Different Description";

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(409)
                .body("error", equalTo("Gender with code 'A' already exists"));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/genders - Should reject duplicate description")
    void testCreateGenderDuplicateDescription() {
        // First create a gender
        Gender firstGender = new Gender();
        firstGender.code = "B";
        firstGender.description = "Test Description Duplicate";

        given()
                .contentType(ContentType.JSON)
                .body(firstGender)
                .when().post("/api/genders")
                .then()
                .statusCode(201);

        // Try to create another with same description
        Gender gender = new Gender();
        gender.code = "C";
        gender.description = "Test Description Duplicate"; // Same as already created

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(409)
                .body("error", equalTo("Gender with description 'Test Description Duplicate' already exists"));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/genders - Should reject null code")
    void testCreateGenderNullCode() {
        Gender gender = new Gender();
        gender.code = null;
        gender.description = "Test Description";

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/genders - Should reject empty code")
    void testCreateGenderEmptyCode() {
        Gender gender = new Gender();
        gender.code = "";
        gender.description = "Test Description";

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/genders - Should reject invalid code format")
    void testCreateGenderInvalidCodeFormat() {
        Gender gender = new Gender();
        gender.code = "m"; // lowercase
        gender.description = "Test Description";

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/genders - Should reject multi-character code")
    void testCreateGenderMultiCharacterCode() {
        Gender gender = new Gender();
        gender.code = "AB";
        gender.description = "Test Description";

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(10)
    @DisplayName("POST /api/genders - Should reject null description")
    void testCreateGenderNullDescription() {
        Gender gender = new Gender();
        gender.code = "T";
        gender.description = null;

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/genders - Should reject empty description")
    void testCreateGenderEmptyDescription() {
        Gender gender = new Gender();
        gender.code = "T";
        gender.description = "";

        given()
                .contentType(ContentType.JSON)
                .body(gender)
                .when().post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/genders/{id} - Should return gender by ID")
    void testGetGenderById() {
        Assumptions.assumeTrue(createdGenderId != null, "Created gender ID should not be null");
        
        given()
                .when().get("/api/genders/{id}", createdGenderId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(createdGenderId.intValue()))
                .body("code", equalTo(VALID_CODE))
                .body("description", equalTo(VALID_DESCRIPTION));
    }

    @Test
    @Order(13)
    @DisplayName("GET /api/genders/{id} - Should return 404 for non-existent ID")
    void testGetGenderByIdNotFound() {
        given()
                .when().get("/api/genders/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", equalTo("Entity not found with id: 99999"));
    }

    @Test
    @Order(14)
    @DisplayName("GET /api/genders/code/{code} - Should return gender by code")
    void testGetGenderByCode() {
        // Ensure we have a gender to find by creating it if not exists or using the one we know exists
        if (createdGenderId != null) {
            given()
                    .when().get("/api/genders/code/{code}", "V")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("code", equalTo("V"))
                    .body("description", notNullValue());
        } else {
            // Create a gender for this test
            Gender testGender = new Gender();
            testGender.code = "Z";
            testGender.description = "Test Gender for Code Search";

            given()
                    .contentType(ContentType.JSON)
                    .body(testGender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201);

            given()
                    .when().get("/api/genders/code/{code}", "Z")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("code", equalTo("Z"))
                    .body("description", equalTo("Test Gender for Code Search"));
        }
    }

    @Test
    @Order(15)
    @DisplayName("GET /api/genders/code/{code} - Should return 404 for non-existent code")
    void testGetGenderByCodeNotFound() {
        given()
                .when().get("/api/genders/code/{code}", "NONEXISTENT")
                .then()
                .statusCode(404)
                .body("error", equalTo("Entity not found with code: NONEXISTENT"));
    }

    @Test
    @Order(16)
    @DisplayName("PUT /api/genders/{id} - Should update gender successfully")
    void testUpdateGender() {
        Assumptions.assumeTrue(createdGenderId != null, "Created gender ID should not be null");
        
        Gender updateGender = new Gender();
        updateGender.code = "V"; // Use unique code for update
        updateGender.description = "Updated Test Gender"; // Use unique description

        given()
                .contentType(ContentType.JSON)
                .body(updateGender)
                .when().put("/api/genders/{id}", createdGenderId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(createdGenderId.intValue()))
                .body("code", equalTo("V"))
                .body("description", equalTo("Updated Test Gender"))
                .body("updatedAt", notNullValue());
    }

    @Test
    @Order(17)
    @DisplayName("PUT /api/genders/{id} - Should return 404 for non-existent ID")
    void testUpdateGenderNotFound() {
        Gender updateGender = new Gender();
        updateGender.code = "X";
        updateGender.description = "Test Description";

        given()
                .contentType(ContentType.JSON)
                .body(updateGender)
                .when().put("/api/genders/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", equalTo("Entity not found with id: 99999"));
    }

    @Test
    @Order(18)
    @DisplayName("PUT /api/genders/{id} - Should reject duplicate code")
    void testUpdateGenderDuplicateCode() {
        Assumptions.assumeTrue(createdGenderId != null, "Created gender ID should not be null");
        
        // First create another gender
        Gender anotherGender = new Gender();
        anotherGender.code = "X";
        anotherGender.description = "Another Gender";

        Integer anotherIdAsInteger = given()
                .contentType(ContentType.JSON)
                .body(anotherGender)
                .when().post("/api/genders")
                .then()
                .statusCode(201)
                .extract().path("id");
        

        // Try to update the first gender with the same code as the second
        Gender updateGender = new Gender();
        updateGender.code = "X"; // Same as anotherGender
        updateGender.description = "Updated Description";

        given()
                .contentType(ContentType.JSON)
                .body(updateGender)
                .when().put("/api/genders/{id}", createdGenderId)
                .then()
                .statusCode(409)
                .body("error", equalTo("Another gender with code 'X' already exists"));
    }

    @Test
    @Order(19)
    @DisplayName("PUT /api/genders/{id} - Should update with valid data")
    void testUpdateGenderPartial() {
        Assumptions.assumeTrue(createdGenderId != null, "Created gender ID should not be null");
        
        // Since @Valid is used, we need to provide valid complete object
        Gender updateGender = new Gender();
        updateGender.code = "W"; // Valid code
        updateGender.description = "Final Updated Test Description";

        given()
                .contentType(ContentType.JSON)
                .body(updateGender)
                .when().put("/api/genders/{id}", createdGenderId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo("W"))
                .body("description", equalTo("Final Updated Test Description"));
    }

    @Test
    @Order(20)
    @DisplayName("DELETE /api/genders/{id} - Should delete gender successfully")
    void testDeleteGender() {
        Assumptions.assumeTrue(createdGenderId != null, "Created gender ID should not be null");
        
        given()
                .when().delete("/api/genders/{id}", createdGenderId)
                .then()
                .statusCode(204);

        // Verify it's deleted
        given()
                .when().get("/api/genders/{id}", createdGenderId)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(21)
    @DisplayName("DELETE /api/genders/{id} - Should return 404 for non-existent ID")
    void testDeleteGenderNotFound() {
        given()
                .when().delete("/api/genders/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", equalTo("Entity not found with id: 99999"));
    }
}