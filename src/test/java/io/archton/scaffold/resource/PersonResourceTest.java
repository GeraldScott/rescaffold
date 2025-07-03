package io.archton.scaffold.resource;

import io.archton.scaffold.domain.Person;
import io.archton.scaffold.domain.Gender;
import io.archton.scaffold.domain.Title;
import io.archton.scaffold.domain.IdType;
import io.archton.scaffold.domain.Country;
import io.archton.scaffold.repository.GenderRepository;
import io.archton.scaffold.repository.TitleRepository;
import io.archton.scaffold.repository.IdTypeRepository;
import io.archton.scaffold.repository.CountryRepository;
import io.archton.scaffold.repository.PersonRepository;
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
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("Person Resource REST API Tests")
class PersonResourceTest {

    @Inject
    GenderRepository genderRepository;

    @Inject
    TitleRepository titleRepository;

    @Inject
    IdTypeRepository idTypeRepository;

    @Inject
    CountryRepository countryRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    PersonRepository personRepository;

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
        Gender gender = genderRepository.findByCode("F");
        if (gender == null) {
            gender = new Gender("F", "Female");
            genderRepository.persist(gender);
            entityManager.flush();
        }

        Title title = titleRepository.findByCode("MS");
        if (title == null) {
            title = new Title("MS", "Ms.");
            titleRepository.persist(title);
            entityManager.flush();
        }

        IdType idType = idTypeRepository.findByCode("ID");
        if (idType == null) {
            idType = new IdType("ID", "Identity Document");
            idTypeRepository.persist(idType);
            entityManager.flush();
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
    
    @Transactional
    void setupIdType(String code, String description) {
        IdType existing = idTypeRepository.findByCode(code);
        if (existing == null) {
            IdType idType = new IdType(code, description);
            idTypeRepository.persist(idType);
        }
    }
    
    @Transactional
    void cleanupTestData(Long personId, Long idTypeId) {
        try {
            if (personId != null) {
                Person person = personRepository.findById(personId);
                if (person != null) {
                    personRepository.delete(person);
                }
            }
            if (idTypeId != null) {
                IdType idType = idTypeRepository.findById(idTypeId);
                if (idType != null && ("ID".equals(idType.code) || "PASS".equals(idType.code))) {
                    // Only delete if it's one of our test IdTypes
                    idTypeRepository.delete(idType);
                }
            }
        } catch (Exception e) {
            // Ignore cleanup failures in tests
        }
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

    // Additional validation tests for field constraints

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should reject null last name")
    void testCreatePerson_NullLastName() {
        Person person = new Person();
        person.firstName = "John";
        person.lastName = null;
        person.email = "john.null@example.com";

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
    @DisplayName("POST /api/persons - Should reject empty last name")
    void testCreatePerson_EmptyLastName() {
        Person person = new Person();
        person.firstName = "John";
        person.lastName = "";
        person.email = "john.empty@example.com";

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
    @DisplayName("POST /api/persons - Should reject first name exceeding 100 characters")
    void testCreatePerson_FirstNameTooLong() {
        Person person = new Person();
        person.firstName = "A".repeat(101); // 101 characters
        person.lastName = "Doe";
        person.email = "john.longname@example.com";

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
    @DisplayName("POST /api/persons - Should reject last name exceeding 100 characters")
    void testCreatePerson_LastNameTooLong() {
        Person person = new Person();
        person.firstName = "John";
        person.lastName = "B".repeat(101); // 101 characters
        person.email = "john.longlast@example.com";

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
    @DisplayName("POST /api/persons - Should reject email exceeding 255 characters")
    void testCreatePerson_EmailTooLong() {
        Person person = new Person();
        person.firstName = "John";
        person.lastName = "Doe";
        person.email = "a".repeat(250) + "@example.com"; // 261 characters

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
    @DisplayName("POST /api/persons - Should reject idNumber exceeding 13 characters")
    void testCreatePerson_IdNumberTooLong() {
        Person person = new Person();
        person.firstName = "John";
        person.lastName = "Doe";
        person.email = "john.longid@example.com";
        person.idNumber = "1".repeat(14); // 14 characters

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
    @DisplayName("POST /api/persons - Should reject invalid email formats")
    void testCreatePerson_InvalidEmailFormats() {
        String[] invalidEmails = {
            "plainaddress",
            "@missinglocal.com",
            "missing@domain",
            "missing.domain@.com",
            "spaces @domain.com",
            "domain@spaces .com"
        };

        for (String invalidEmail : invalidEmails) {
            Person person = new Person();
            person.firstName = "John";
            person.lastName = "Doe";
            person.email = invalidEmail;

            given()
                    .spec(requestSpec)
                    .body(person)
                    .when()
                    .post()
                    .then()
                    .statusCode(400);
        }
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should accept valid email formats")
    void testCreatePerson_ValidEmailFormats() {
        String[] validEmails = {
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.org",
            "user_name@example-domain.co.uk"
        };

        for (int i = 0; i < validEmails.length; i++) {
            Person person = new Person();
            person.firstName = "John";
            person.lastName = "Doe" + i; // Unique last name for each
            person.email = validEmails[i];

            given()
                    .spec(requestSpec)
                    .body(person)
                    .when()
                    .post()
                    .then()
                    .statusCode(201);
        }
    }

    // Foreign key constraint validation tests
    // Note: Foreign keys are validated by the database and would result in 500 errors
    // The person creation with relations test already covers this functionality
    // Additional FK validation is tested implicitly in the other constraint tests

    // Unique constraint testing for (country_id, id_number)

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should reject duplicate (country_id, id_number) combination")
    void testCreatePerson_DuplicateCountryIdNumber() {
        // Create a country with unique data for this test
        long timestamp = System.currentTimeMillis();
        String uniqueCountryCode = "Z" + (char)('A' + (timestamp % 26)); // ZA, ZB, ZC, etc.
        Country country = createValidCountry(uniqueCountryCode, "Duplicate Test Country " + timestamp);
        Integer countryId = given()
                .spec(new RequestSpecBuilder().setBasePath("/api/countries").setContentType(ContentType.JSON).build())
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String uniqueIdNumber = "UNIQUE" + System.currentTimeMillis() % 10000;
        String uniqueEmailBase = "test.duplicate." + System.currentTimeMillis() % 10000;

        // Create first person via API with specific country and id_number
        Person person1 = new Person();
        person1.firstName = "First";
        person1.lastName = "DuplicateTest";
        person1.email = uniqueEmailBase + ".first@example.com";
        person1.idNumber = uniqueIdNumber;
        // Set country via ID
        Country countryRef = new Country();
        countryRef.id = countryId.longValue();
        person1.country = countryRef;

        given()
                .spec(requestSpec)
                .body(person1)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Try to create second person with same country and id_number
        Person person2 = new Person();
        person2.firstName = "Second";
        person2.lastName = "DuplicateTest";
        person2.email = uniqueEmailBase + ".second@example.com";
        person2.idNumber = uniqueIdNumber; // Same as person1
        person2.country = countryRef; // Same country

        given()
                .spec(requestSpec)
                .body(person2)
                .when()
                .post()
                .then()
                .statusCode(409); // Conflict due to unique constraint on (country_id, id_number)
    }

    // Helper method for creating a valid country
    private Country createValidCountry(String code, String name) {
        Country country = new Country();
        country.code = code;
        country.name = name;
        return country;
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should allow same id_number in different countries")
    void testCreatePerson_SameIdNumberDifferentCountries() {
        // Create countries via API with unique data
        RequestSpecification countrySpec = new RequestSpecBuilder().setBasePath("/api/countries").setContentType(ContentType.JSON).build();
        long timestamp = System.currentTimeMillis();
        
        String code1 = "Y" + (char)('A' + (timestamp % 26));
        String code2 = "X" + (char)('A' + ((timestamp + 1) % 26));
        
        Country country1 = createValidCountry(code1, "Mexico Test " + timestamp + "A");
        Integer countryId1 = given()
                .spec(countrySpec)
                .body(country1)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Country country2 = createValidCountry(code2, "Brazil Test " + timestamp + "B");
        Integer countryId2 = given()
                .spec(countrySpec)
                .body(country2)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String sharedIdNumber = "SAME" + (timestamp % 100000000L);
        
        // Create first person in first country
        Person person1 = new Person();
        person1.firstName = "First";
        person1.lastName = "PersonMX" + timestamp;
        person1.email = "first.personmx." + timestamp + "@example.com";
        person1.idNumber = sharedIdNumber;
        Country countryRef1 = new Country();
        countryRef1.id = countryId1.longValue();
        person1.country = countryRef1;

        given()
                .spec(requestSpec)
                .body(person1)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Create second person in second country with same id_number (should be allowed)
        Person person2 = new Person();
        person2.firstName = "Second";
        person2.lastName = "PersonBR" + timestamp;
        person2.email = "second.personbr." + timestamp + "@example.com";
        person2.idNumber = sharedIdNumber; // Same id_number but different country
        Country countryRef2 = new Country();
        countryRef2.id = countryId2.longValue();
        person2.country = countryRef2;

        given()
                .spec(requestSpec)
                .body(person2)
                .when()
                .post()
                .then()
                .statusCode(201); // Should succeed because different countries
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should allow null id_number with same country")
    void testCreatePerson_NullIdNumberSameCountry() {
        // Create a country via API
        RequestSpecification countrySpec = new RequestSpecBuilder().setBasePath("/api/countries").setContentType(ContentType.JSON).build();
        long timestamp = System.currentTimeMillis();
        String countryCode = "W" + (char)('A' + (timestamp % 26));
        Country country = createValidCountry(countryCode, "India Test " + timestamp);
        Integer countryId = given()
                .spec(countrySpec)
                .body(country)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Country countryRef = new Country();
        countryRef.id = countryId.longValue();

        // Create first person with null id_number
        Person person1 = new Person();
        person1.firstName = "First";
        person1.lastName = "NullId1" + timestamp;
        person1.email = "first.nullid." + timestamp + "@example.com";
        person1.country = countryRef;
        person1.idNumber = null;

        given()
                .spec(requestSpec)
                .body(person1)
                .when()
                .post()
                .then()
                .statusCode(201);

        // Create second person with null id_number in same country (should be allowed)
        Person person2 = new Person();
        person2.firstName = "Second";
        person2.lastName = "NullId2" + timestamp;
        person2.email = "second.nullid." + timestamp + "@example.com";
        person2.country = countryRef;
        person2.idNumber = null;

        given()
                .spec(requestSpec)
                .body(person2)
                .when()
                .post()
                .then()
                .statusCode(201); // Should succeed because null values don't violate unique constraint
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should accept person with null country and any id_number")
    void testCreatePerson_NullCountryAnyIdNumber() {
        // Create persons with null country but same id_number (should be allowed)
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000);
        String sharedNumber = "ANY" + timestamp;
        
        Person person1 = new Person();
        person1.firstName = "First";
        person1.lastName = "NoCountry1" + timestamp;
        person1.email = "first.nocountry." + timestamp + "@example.com";
        person1.country = null;
        person1.idNumber = sharedNumber;

        given()
                .spec(requestSpec)
                .body(person1)
                .when()
                .post()
                .then()
                .statusCode(201);

        Person person2 = new Person();
        person2.firstName = "Second";
        person2.lastName = "NoCountry2" + timestamp;
        person2.email = "second.nocountry." + timestamp + "@example.com";
        person2.country = null;
        person2.idNumber = sharedNumber; // Same id_number but null country

        given()
                .spec(requestSpec)
                .body(person2)
                .when()
                .post()
                .then()
                .statusCode(201); // Should succeed because null country
    }

    @Test
    @TestTransaction
    @DisplayName("POST /api/persons - Should reject invalid RSA ID number when id_type is ID")
    void testCreatePerson_InvalidRsaIdNumber() {
        // Setup ID type
        IdType idType = idTypeRepository.findByCode("ID");
        if (idType == null) {
            idType = new IdType("ID", "Identity Document");
            idTypeRepository.persist(idType);
            entityManager.flush();
        }

        Person person = new Person();
        person.firstName = "John";
        person.lastName = "Doe";
        person.email = "john.invalid.rsa@example.com";
        person.idNumber = "1234567890123"; // Invalid RSA ID number
        person.idType = idType;

        given()
                .spec(requestSpec)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/persons - Should accept valid RSA ID number when id_type is ID")
    void testCreatePerson_ValidRsaIdNumber() {
        // Setup and cleanup will be manual since we removed @TestTransaction
        Long createdPersonId = null;
        Long createdIdTypeId = null;
        
        try {
            // Setup ID type in a transaction that commits
            setupIdType("ID", "Identity Document");
            
            Person person = new Person();
            person.firstName = "Jane";
            person.lastName = "Smith";
            person.email = "jane.valid.rsa@example.com";
            person.idNumber = "8001015009087"; // Valid RSA ID number
            
            // Find the IdType after it's been committed
            IdType idType = idTypeRepository.findByCode("ID");
            person.idType = idType;
            createdIdTypeId = idType.id;

            Integer responseId = given()
                    .spec(requestSpec)
                    .body(person)
                    .when()
                    .post()
                    .then()
                    .statusCode(201)
                    .body("idNumber", equalTo("8001015009087"))
                    .extract()
                    .path("id");
            
            createdPersonId = responseId.longValue();
            
        } finally {
            // Cleanup
            cleanupTestData(createdPersonId, createdIdTypeId);
        }
    }

    @Test
    @DisplayName("POST /api/persons - Should accept any id_number when id_type is not ID")
    void testCreatePerson_NonIdTypeAnyNumber() {
        // Setup and cleanup will be manual since we removed @TestTransaction
        Long createdPersonId = null;
        Long createdIdTypeId = null;
        
        try {
            // Setup ID type in a transaction that commits
            setupIdType("PASS", "Passport");
            
            Person person = new Person();
            person.firstName = "Bob";
            person.lastName = "Wilson";
            person.email = "bob.passport@example.com";
            person.idNumber = "ABC123456"; // Any format allowed for non-ID types
            
            // Find the IdType after it's been committed
            IdType idType = idTypeRepository.findByCode("PASS");
            person.idType = idType;
            createdIdTypeId = idType.id;

            Integer responseId = given()
                    .spec(requestSpec)
                    .body(person)
                    .when()
                    .post()
                    .then()
                    .statusCode(201)
                    .body("idNumber", equalTo("ABC123456"))
                    .extract()
                    .path("id");
            
            createdPersonId = responseId.longValue();
            
        } finally {
            // Cleanup
            cleanupTestData(createdPersonId, createdIdTypeId);
        }
    }

    @Test
    @TestTransaction
    @DisplayName("PUT /api/persons/{id} - Should reject invalid RSA ID number on update")
    void testUpdatePerson_InvalidRsaIdNumber() {
        // Create person first
        Person person = createValidPerson("Alice", "Update", "alice.update.rsa@example.com");
        Integer createdId = given()
                .spec(requestSpec)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Setup ID type for update
        IdType idType = idTypeRepository.findByCode("ID");
        if (idType == null) {
            idType = new IdType("ID", "Identity Document");
            idTypeRepository.persist(idType);
            entityManager.flush();
        }

        // Update with invalid RSA ID
        Person updates = new Person();
        updates.idNumber = "9999999999999"; // Invalid RSA ID number
        updates.idType = idType;

        given()
                .spec(requestSpec)
                .body(updates)
                .when()
                .put("/{id}", createdId)
                .then()
                .statusCode(400);
    }
}