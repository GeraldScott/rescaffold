package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Country;
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
@DisplayName("Country Resource REST API Tests")
class CountryResourceTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    @BeforeEach
    void setUp() {
        // Common request specification for all tests
        requestSpec = new RequestSpecBuilder()
                .setBasePath("/api/countries")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        // Common response specification for successful responses
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }

    // Helper methods for creating test data
    private Country createValidCountry(String code, String name) {
        Country country = new Country();
        country.code = code;
        country.name = name;
        return country;
    }

    private Country createValidCountryWithOptionalFields(String code, String name, String year, String cctld) {
        Country country = new Country();
        country.code = code;
        country.name = name;
        country.year = year;
        country.cctld = cctld;
        return country;
    }

    private Country createInvalidCountryWithTooLongCode() {
        Country country = new Country();
        country.code = "USA"; // Invalid: must be exactly 2 characters
        country.name = "United States Invalid";
        return country;
    }

    private Country createInvalidCountryWithTooShortCode() {
        Country country = new Country();
        country.code = "U"; // Invalid: must be exactly 2 characters
        country.name = "United States Short";
        return country;
    }

    private Country createInvalidCountryWithLowercaseCode() {
        Country country = new Country();
        country.code = "us"; // Invalid: must be uppercase
        country.name = "United States Lower";
        return country;
    }

    private Country createInvalidCountryWithNumericCode() {
        Country country = new Country();
        country.code = "U1"; // Invalid: must be alphabetic
        country.name = "United States Numeric";
        return country;
    }

    // CRUD operation tests

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should create country successfully")
    void testCreateCountry_ValidData() {
        Country country = createValidCountry("US", "United States");

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .spec(responseSpec)
                .statusCode(201)
                .body("id", notNullValue())
                .body("code", equalTo("US"))
                .body("name", equalTo("United States"))
                .body("createdBy", equalTo("system"))
                .body("createdAt", notNullValue())
                .body("updatedBy", nullValue())
                .body("updatedAt", nullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should create country with optional fields")
    void testCreateCountry_WithOptionalFields() {
        Country country = createValidCountryWithOptionalFields("CA", "Canada", "1867", ".ca");

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .spec(responseSpec)
                .statusCode(201)
                .body("id", notNullValue())
                .body("code", equalTo("CA"))
                .body("name", equalTo("Canada"))
                .body("year", equalTo("1867"))
                .body("cctld", equalTo(".ca"));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/countries/{id} - Should return country by ID")
    void testGetCountryById_ValidId() {
        // First create a country
        Country country = createValidCountry("GB", "United Kingdom");
        Integer createdId = given()
                .spec(requestSpec)
                .body(country)
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
                .body("code", equalTo("GB"))
                .body("name", equalTo("United Kingdom"));
    }

    @Test
    @TestTransaction
    @DisplayName("GET /api/countries/code/{code} - Should return country by code")
    void testGetCountryByCode_ValidCode() {
        // First create a country
        Country country = createValidCountry("DE", "Germany");
        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Then retrieve it by code
        given()
                .spec(requestSpec)
                .when()
                .get("/code/{code}", "DE")
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("code", equalTo("DE"))
                .body("name", equalTo("Germany"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/countries/{id} - Should update country successfully")
    void testUpdateCountry_ValidData() {
        // First create a country
        Country country = createValidCountry("FR", "France");
        Integer createdId = given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then update it
        Country updatedCountry = createValidCountryWithOptionalFields("FR", "French Republic", "1958", ".fr");
        given()
                .spec(requestSpec)
                .body(updatedCountry)
                .when()
                .put("/{id}", createdId)
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("id", equalTo(createdId))
                .body("code", equalTo("FR"))
                .body("name", equalTo("French Republic"))
                .body("year", equalTo("1958"))
                .body("cctld", equalTo(".fr"))
                .body("updatedAt", notNullValue());
    }

    @Test
    @TestTransaction
    @DisplayName("DELETE /api/countries/{id} - Should delete country successfully")
    void testDeleteCountry_ValidId() {
        // First create a country
        Country country = createValidCountry("ES", "Spain");
        Integer createdId = given()
                .spec(requestSpec)
                .body(country)
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
    @DisplayName("GET /api/countries - Should return populated list after creating countries")
    void testGetAllCountries_PopulatedList() {
        // Create multiple countries with unique codes for this test
        given().spec(requestSpec).body(createValidCountry("IT", "Italy")).when().post().then().statusCode(201);
        given().spec(requestSpec).body(createValidCountry("JP", "Japan")).when().post().then().statusCode(201);
        given().spec(requestSpec).body(createValidCountry("AU", "Australia")).when().post().then().statusCode(201);

        // Verify list contains our created countries (size may vary due to test isolation issues)
        given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(3)))
                .body("code", hasItems("IT", "JP", "AU"))
                .body("name", hasItems("Italy", "Japan", "Australia"));
    }

    // Data validation tests

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject null code")
    void testCreateCountry_NullCode() {
        Country country = new Country();
        country.code = null;
        country.name = "No Code";

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject empty code")
    void testCreateCountry_EmptyCode() {
        Country country = new Country();
        country.code = "";
        country.name = "Empty Code";

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject blank code")
    void testCreateCountry_BlankCode() {
        Country country = new Country();
        country.code = "  ";
        country.name = "Blank Code";

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject code longer than 2 characters")
    void testCreateCountry_TooLongCode() {
        Country country = createInvalidCountryWithTooLongCode();

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject code shorter than 2 characters")
    void testCreateCountry_TooShortCode() {
        Country country = createInvalidCountryWithTooShortCode();

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject lowercase code")
    void testCreateCountry_LowercaseCode() {
        Country country = createInvalidCountryWithLowercaseCode();

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject numeric code")
    void testCreateCountry_NumericCode() {
        Country country = createInvalidCountryWithNumericCode();

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject null name")
    void testCreateCountry_NullName() {
        Country country = new Country();
        country.code = "XY";
        country.name = null;

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject empty name")
    void testCreateCountry_EmptyName() {
        Country country = new Country();
        country.code = "XY";
        country.name = "";

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject blank name")
    void testCreateCountry_BlankName() {
        Country country = new Country();
        country.code = "XY";
        country.name = "   ";

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject when ID is provided")
    void testCreateCountry_WithId() {
        Country country = createValidCountry("XX", "Test Country");
        country.id = 999L; // Should not be provided in POST

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("error", containsString("ID must not be included"));
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/countries - Should reject duplicate code")
    void testCreateCountry_DuplicateCode() {
        // Create first country with unique code for this test
        Country first = createValidCountry("DK", "Denmark");
        given()
                .spec(requestSpec)
                .body(first)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Try to create second with same code
        Country duplicate = createValidCountry("DK", "Different Denmark");
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
    @DisplayName("POST /api/countries - Should reject duplicate name")
    void testCreateCountry_DuplicateName() {
        // Create first country with unique data for this test
        Country first = createValidCountry("NO", "Norway Unique Test");
        given()
                .spec(requestSpec)
                .body(first)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Try to create second with same name
        Country duplicate = createValidCountry("SE", "Norway Unique Test");
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
    @DisplayName("POST /api/countries - Should properly process valid uppercase code")
    void testCreateCountry_ValidUppercaseCode() {
        Country country = createValidCountry("FI", "Finland");

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .post()
                .then()
                .spec(responseSpec)
                .statusCode(201)
                .body("code", equalTo("FI"))
                .body("name", equalTo("Finland"));
    }

    // Error handling tests

    @Test
    @TestTransaction
    @DisplayName("GET /api/countries/{id} - Should return 404 for non-existent ID")
    void testGetCountryById_NotFound() {
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
    @DisplayName("GET /api/countries/code/{code} - Should return 404 for non-existent code")
    void testGetCountryByCode_NotFound() {
        given()
                .spec(requestSpec)
                .when()
                .get("/code/{code}", "ZZ")
                .then()
                .statusCode(404)
                .body("error", containsString("Entity not found"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/countries/{id} - Should return 404 for non-existent ID")
    void testUpdateCountry_NotFound() {
        Country country = createValidCountry("XX", "Non-existent Country");

        given()
                .spec(requestSpec)
                .body(country)
                .when()
                .put("/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", containsString("Entity not found"));
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/countries/{id} - Should return 409 for duplicate code")
    void testUpdateCountry_DuplicateCode() {
        // Create two countries with unique codes for this test
        Country first = createValidCountry("PL", "Poland");
        Integer firstId = given().spec(requestSpec).body(first).when().post().then().statusCode(201).extract().path("id");

        Country second = createValidCountry("CZ", "Czech Republic");
        given().spec(requestSpec).body(second).when().post().then().statusCode(201);

        // Try to update first with second's code
        Country update = createValidCountry("CZ", "Updated Poland");
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
    @DisplayName("PUT /api/countries/{id} - Should return 409 for duplicate name")
    void testUpdateCountry_DuplicateName() {
        // Create two countries with unique data for this test
        Country first = createValidCountry("SK", "Slovakia");
        Integer firstId = given().spec(requestSpec).body(first).when().post().then().statusCode(201).extract().path("id");

        Country second = createValidCountry("SI", "Slovenia");
        given().spec(requestSpec).body(second).when().post().then().statusCode(201);

        // Try to update first with second's name
        Country update = createValidCountry("SK", "Slovenia");
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
    @DisplayName("DELETE /api/countries/{id} - Should return 404 for non-existent ID")
    void testDeleteCountry_NotFound() {
        given()
                .spec(requestSpec)
                .when()
                .delete("/{id}", 99999L)
                .then()
                .statusCode(404)
                .body("error", containsString("Entity not found"));
    }
}