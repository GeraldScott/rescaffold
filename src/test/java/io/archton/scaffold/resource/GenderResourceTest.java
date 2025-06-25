package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Gender;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Gender Resource API Tests")
class GenderResourceTest {

    private static final Set<String> TEST_CODES = Set.of("T", "X", "Y", "Z", "G", "C", "U", "V", "D", "E", "P", "R", "A", "B", "I");
    private static final Long NON_EXISTENT_ID = 99999L;
    private static final String NON_EXISTENT_CODE = "Q";

    @BeforeAll
    @TestTransaction
    void setupDatabase() {
        // Clean up any leftover test data from previous runs
        cleanupTestData();

        // Optionally ensure we have the expected base data
        ensureBaseDataExists();
    }

    @BeforeEach
    @TestTransaction
    void cleanupBeforeTest() {
        // Clean up any test data that might exist from failed tests
        Gender.delete("createdBy = ?1 OR code in ?2", "test-user", TEST_CODES);
    }

    @AfterEach
    @TestTransaction
    void cleanupAfterTest() {
        // Extra safety net - clean up test data after each test
        cleanupTestData();
    }

    private void cleanupTestData() {
        // Delete only test data by specific codes
        Gender.delete("code in ?1", TEST_CODES);

    }

    private void ensureBaseDataExists() {
        // Ensure the expected base data exists (F, M, O, U)
        if (Gender.count("code in ('F', 'M', 'O', 'U')") < 4) {
            // Recreate base data if missing
            createBaseDataIfMissing();
        }
    }

    private void createBaseDataIfMissing() {
        String[][] baseData = {
            {"F", "Female"},
            {"M", "Male"},
            {"O", "Other"},
            {"U", "Unknown"}
        };

        for (String[] data : baseData) {
            if (Gender.findByCode(data[0]) == null) {
                Gender gender = new Gender(data[0], data[1]);
                gender.persist();
            }
        }
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
                "description", "Test Gender",
                "createdBy", "test-user" // Mark as test data
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
                .body("id", notNullValue());
    }

    // Helper method to create test gender with guaranteed unique data
    private Gender createTestGender(String suffix) {
        Map<String, Object> genderData = Map.of(
                "code", suffix,
                "description", "Test Gender " + suffix,
                "createdBy", "test-user"
        );

        var response = given()
                .contentType(ContentType.JSON)
                .body(genderData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(201)
                .extract().response();

        Long id = response.jsonPath().getLong("id");
        return Gender.findById(id);
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/{id} - should return gender by ID")
    void shouldReturnGenderById() {
        // Create test gender
        Gender testGender = createTestGender("X");
        
        given()
                .when()
                .get("/api/genders/" + testGender.id)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(testGender.id.intValue()))
                .body("code", equalTo("X"))
                .body("description", equalTo("Test Gender X"))
                .body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/{id} - should return 404 for non-existent ID")
    void shouldReturn404ForNonExistentId() {
        given()
                .when()
                .get("/api/genders/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", equalTo("Entity not found with id: " + NON_EXISTENT_ID));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/code/{code} - should return gender by code")
    void shouldReturnGenderByCode() {
        // Create test gender
        createTestGender("Y");
        
        given()
                .when()
                .get("/api/genders/code/Y")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo("Y"))
                .body("description", equalTo("Test Gender Y"))
                .body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/genders/code/{code} - should return 404 for non-existent code")
    void shouldReturn404ForNonExistentCode() {
        given()
                .when()
                .get("/api/genders/code/" + NON_EXISTENT_CODE)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", equalTo("Entity not found with code: " + NON_EXISTENT_CODE));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - should update gender successfully")
    void shouldUpdateGenderSuccessfully() {
        // Create initial test gender
        Gender testGender = createTestGender("Z");
        
        Map<String, Object> updateData = Map.of(
                "code", "A",
                "description", "Updated Gender A",
                "isActive", true
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/api/genders/" + testGender.id)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(testGender.id.intValue()))
                .body("code", equalTo("A"))
                .body("description", equalTo("Updated Gender A"))
                .body("isActive", equalTo(true));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - should return 404 for non-existent ID")
    void shouldReturn404ForUpdateNonExistentId() {
        Map<String, Object> updateData = Map.of(
                "code", "B",
                "description", "Updated Gender B"
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/api/genders/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", equalTo("Entity not found with id: " + NON_EXISTENT_ID));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/genders/{id} - should return 409 for duplicate code")
    void shouldReturn409ForDuplicateCodeOnUpdate() {
        // Create two test genders
        Gender gender1 = createTestGender("C");
        Gender gender2 = createTestGender("D");
        
        // Try to update gender2 to use gender1's code
        Map<String, Object> updateData = Map.of(
                "code", "C",
                "description", "Updated Description"
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/api/genders/" + gender2.id)
                .then()
                .statusCode(409)
                .contentType(ContentType.JSON)
                .body("error", containsString("Another gender with code 'C' already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/genders/{id} - should delete gender successfully")
    void shouldDeleteGenderSuccessfully() {
        // Create test gender
        Gender testGender = createTestGender("E");
        
        given()
                .when()
                .delete("/api/genders/" + testGender.id)
                .then()
                .statusCode(204);
                
        // Verify gender is deleted
        given()
                .when()
                .get("/api/genders/" + testGender.id)
                .then()
                .statusCode(404);
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/genders/{id} - should return 404 for non-existent ID")
    void shouldReturn404ForDeleteNonExistentId() {
        given()
                .when()
                .delete("/api/genders/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", equalTo("Entity not found with id: " + NON_EXISTENT_ID));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should return 409 for duplicate code")
    void shouldReturn409ForDuplicateCode() {
        // Create first gender
        createTestGender("G");
        
        // Try to create another with same code
        Map<String, Object> duplicateData = Map.of(
                "code", "G",
                "description", "Another Test Gender",
                "createdBy", "test-user"
        );

        given()
                .contentType(ContentType.JSON)
                .body(duplicateData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(409)
                .contentType(ContentType.JSON)
                .body("error", containsString("Gender with code 'G' already exists"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should return 400 for invalid code")
    void shouldReturn400ForInvalidCode() {
        Map<String, Object> invalidData = Map.of(
                "code", "1", // Invalid: must be alphabetic
                "description", "Invalid Gender",
                "createdBy", "test-user"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should return 400 for blank code")
    void shouldReturn400ForBlankCode() {
        Map<String, Object> invalidData = Map.of(
                "code", "",
                "description", "Blank Code Gender",
                "createdBy", "test-user"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/genders - should return 400 for blank description")
    void shouldReturn400ForBlankDescription() {
        Map<String, Object> invalidData = Map.of(
                "code", "I",
                "description", "",
                "createdBy", "test-user"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidData)
                .when()
                .post("/api/genders")
                .then()
                .statusCode(400);
    }

}