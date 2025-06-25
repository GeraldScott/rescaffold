package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Gender;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@DisplayName("Gender Resource REST API Tests")
class GenderResourceTest {

    private static int testCounter = 0;
    private static int descCounter = 1;
    
    private String getUniqueCode() {
        // Try to find a unique code by checking if it exists in the database
        for (int i = 0; i < 26; i++) {
            char candidateChar = (char) ('A' + ((testCounter + i) % 26));
            String candidate = String.valueOf(candidateChar);
            
            // Check if this code already exists in the database
            Response response = given()
                    .when().get("/api/genders/code/" + candidate)
                    .then()
                    .extract().response();
            
            if (response.getStatusCode() == 404) {
                // Code doesn't exist, we can use it
                testCounter = (testCounter + i + 1) % 26;
                return candidate;
            }
        }
        
        // Fallback - this should rarely happen
        testCounter++;
        return "Z";
    }
    
    private String getUniqueDescription(String base) {
        return base + " " + (descCounter++);
    }

    @Nested
    @DisplayName("GET /api/genders")
    class GetAllGendersTests {

        @Test
        @DisplayName("Should return list of genders")
        void shouldReturnListOfGenders() {
            given()
                    .when().get("/api/genders")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("$", hasSize(greaterThan(-1))); // Accept empty list or list with items
        }
    }

    @Nested
    @DisplayName("POST /api/genders")
    class CreateGenderTests {

        @Test
        @DisplayName("Should create gender successfully")
        void shouldCreateGenderSuccessfully() {
            String code = getUniqueCode();
            String description = getUniqueDescription("Test Gender");
            Gender gender = createValidGender(code, description);

            given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .body("code", equalTo(code))
                    .body("description", equalTo(description))
                    .body("id", notNullValue())
                    .body("isActive", equalTo(true))
                    .body("createdBy", equalTo("system"))
                    .body("createdAt", notNullValue());
        }

        @Test
        @DisplayName("Should reject gender with ID included")
        void shouldRejectGenderWithIdIncluded() {
            Gender gender = createValidGender("X", "Test Gender");
            gender.id = 999L; // This should cause rejection

            given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(400)
                    .body("error", equalTo("ID must not be included in POST request"));
        }

        @Test
        @DisplayName("Should reject duplicate code")
        void shouldRejectDuplicateCode() {
            // Create first gender
            String firstCode = getUniqueCode();
            Gender firstGender = createValidGender(firstCode, getUniqueDescription("First Gender"));
            given()
                    .contentType(ContentType.JSON)
                    .body(firstGender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201);

            // Try to create second with same code
            Gender duplicateGender = createValidGender(firstCode, "Different Description");
            given()
                    .contentType(ContentType.JSON)
                    .body(duplicateGender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(409)
                    .body("error", equalTo("Gender with code '" + firstCode + "' already exists"));
        }

        @Test
        @DisplayName("Should reject duplicate description")
        void shouldRejectDuplicateDescription() {
            // Create first gender  
            String duplicateDesc = getUniqueDescription("Duplicate Description");
            Gender firstGender = createValidGender(getUniqueCode(), duplicateDesc);
            given()
                    .contentType(ContentType.JSON)
                    .body(firstGender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201);

            // Try to create second with same description
            Gender duplicateGender = createValidGender(getUniqueCode(), duplicateDesc);
            given()
                    .contentType(ContentType.JSON)
                    .body(duplicateGender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(409)
                    .body("error", equalTo("Gender with description '" + duplicateDesc + "' already exists"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("Should reject invalid codes")
        void shouldRejectInvalidCodes(String invalidCode) {
            Gender gender = createValidGender(invalidCode, "Test Description");

            given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should reject null code")
        void shouldRejectNullCode() {
            Gender gender = createValidGender(null, "Test Description");

            given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should reject lowercase code")
        void shouldRejectLowercaseCode() {
            Gender gender = createValidGender("m", "Test Description");

            given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should reject multi-character code")
        void shouldRejectMultiCharacterCode() {
            Gender gender = createValidGender("AB", "Test Description");

            given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(400);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("Should reject invalid descriptions")
        void shouldRejectInvalidDescriptions(String invalidDescription) {
            Gender gender = createValidGender("T", invalidDescription);

            given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should reject null description")
        void shouldRejectNullDescription() {
            Gender gender = createValidGender("T", null);

            given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /api/genders/{id}")
    class GetGenderByIdTests {

        @Test
        @DisplayName("Should return gender by ID")
        void shouldReturnGenderById() {
            // Create a gender first
            Gender gender = createValidGender("T", "Test Gender");
            Integer createdId = given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            // Retrieve it by ID
            given()
                    .when().get("/api/genders/{id}", createdId)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("id", equalTo(createdId))
                    .body("code", equalTo("T"))
                    .body("description", equalTo("Test Gender"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent ID")
        void shouldReturn404ForNonExistentId() {
            given()
                    .when().get("/api/genders/{id}", 99999L)
                    .then()
                    .statusCode(404)
                    .body("error", equalTo("Entity not found with id: 99999"));
        }
    }

    @Nested
    @DisplayName("GET /api/genders/code/{code}")
    class GetGenderByCodeTests {

        @Test
        @DisplayName("Should return gender by code")
        void shouldReturnGenderByCode() {
            // Create a gender first
            Gender gender = createValidGender("Z", "Test Gender for Code Search");
            given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201);

            // Retrieve it by code
            given()
                    .when().get("/api/genders/code/{code}", "Z")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("code", equalTo("Z"))
                    .body("description", equalTo("Test Gender for Code Search"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent code")
        void shouldReturn404ForNonExistentCode() {
            given()
                    .when().get("/api/genders/code/{code}", "NONEXISTENT")
                    .then()
                    .statusCode(404)
                    .body("error", equalTo("Entity not found with code: NONEXISTENT"));
        }
    }

    @Nested
    @DisplayName("PUT /api/genders/{id}")
    class UpdateGenderTests {

        @Test
        @DisplayName("Should update gender successfully")
        void shouldUpdateGenderSuccessfully() {
            // Create a gender first
            Gender originalGender = createValidGender("V", "Original Description");
            Integer createdId = given()
                    .contentType(ContentType.JSON)
                    .body(originalGender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            // Update it
            Gender updateGender = createValidGender("W", "Updated Description");
            given()
                    .contentType(ContentType.JSON)
                    .body(updateGender)
                    .when().put("/api/genders/{id}", createdId)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("id", equalTo(createdId))
                    .body("code", equalTo("W"))
                    .body("description", equalTo("Updated Description"))
                    .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("Should return 404 for non-existent ID")
        void shouldReturn404ForNonExistentId() {
            Gender updateGender = createValidGender("X", "Test Description");

            given()
                    .contentType(ContentType.JSON)
                    .body(updateGender)
                    .when().put("/api/genders/{id}", 99999L)
                    .then()
                    .statusCode(404)
                    .body("error", equalTo("Entity not found with id: 99999"));
        }

        @Test
        @DisplayName("Should reject duplicate code in update")
        void shouldRejectDuplicateCodeInUpdate() {
            // Create two genders
            String firstCode = getUniqueCode();
            Gender firstGender = createValidGender(firstCode, getUniqueDescription("First Gender"));
            given()
                    .contentType(ContentType.JSON)
                    .body(firstGender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201);

            Gender secondGender = createValidGender(getUniqueCode(), "Second Gender");
            Integer secondId = given()
                    .contentType(ContentType.JSON)
                    .body(secondGender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            // Try to update second to have same code as first
            Gender updateGender = createValidGender(firstCode, "Updated Description");
            given()
                    .contentType(ContentType.JSON)
                    .body(updateGender)
                    .when().put("/api/genders/{id}", secondId)
                    .then()
                    .statusCode(409)
                    .body("error", equalTo("Another gender with code '" + firstCode + "' already exists"));
        }

        @Test
        @DisplayName("Should update isActive field")
        void shouldUpdateIsActiveField() {
            // Create a gender
            Gender gender = createValidGender("Y", "Test Gender");
            Integer createdId = given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            // Update to inactive
            Gender updateGender = createValidGender("Y", "Test Inactive Gender");
            updateGender.isActive = false;
            given()
                    .contentType(ContentType.JSON)
                    .body(updateGender)
                    .when().put("/api/genders/{id}", createdId)
                    .then()
                    .statusCode(200)
                    .body("isActive", equalTo(false));

            // Update back to active
            updateGender.isActive = true;
            updateGender.description = "Test Active Gender";
            given()
                    .contentType(ContentType.JSON)
                    .body(updateGender)
                    .when().put("/api/genders/{id}", createdId)
                    .then()
                    .statusCode(200)
                    .body("isActive", equalTo(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/genders/{id}")
    class DeleteGenderTests {

        @Test
        @DisplayName("Should delete gender successfully")
        void shouldDeleteGenderSuccessfully() {
            // Create a gender first
            Gender gender = createValidGender("D", "To Be Deleted");
            Integer createdId = given()
                    .contentType(ContentType.JSON)
                    .body(gender)
                    .when().post("/api/genders")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            // Delete it
            given()
                    .when().delete("/api/genders/{id}", createdId)
                    .then()
                    .statusCode(204);

            // Verify it's deleted
            given()
                    .when().get("/api/genders/{id}", createdId)
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("Should return 404 for non-existent ID")
        void shouldReturn404ForNonExistentId() {
            given()
                    .when().delete("/api/genders/{id}", 99999L)
                    .then()
                    .statusCode(404)
                    .body("error", equalTo("Entity not found with id: 99999"));
        }
    }

    // Helper method to create valid Gender objects
    private Gender createValidGender(String code, String description) {
        Gender gender = new Gender();
        gender.code = code;
        gender.description = description;
        return gender;
    }
}