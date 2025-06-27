package io.archton.scaffold.e2e;

import com.codeborne.selenide.SelenideElement;
import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.PersonPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.sleep;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Person CRUD E2E Tests")
class PersonCrudTest extends BaseSelenideTest {

    private final PersonPage personPage = new PersonPage();

    @BeforeEach
    void setUp() {
        // Navigate to persons page before each test
        personPage.openPage();

        // Wait for table to load
        personPage.getPersonTable().should(exist);
    }

    @Test
    @DisplayName("Should display person list table with correct headers")
    void shouldDisplayPersonListTable() {
        // Verify table exists and is visible
        personPage.getPersonTable().should(exist).should(be(visible));
        assertTrue(personPage.isTableVisible(), "Person table should be visible");

        // Verify table headers
        var headers = personPage.getTableHeaders();
        headers.shouldHave(size(8));
        headers.get(0).shouldHave(text("Title"));
        headers.get(1).shouldHave(text("Last Name"));
        headers.get(2).shouldHave(text("First Name"));
        headers.get(3).shouldHave(text("Gender"));
        headers.get(4).shouldHave(text("ID Type"));
        headers.get(5).shouldHave(text("ID Number"));
        headers.get(6).shouldHave(text("Email"));
        headers.get(7).shouldHave(text("Actions"));

        // Verify Create button exists
        personPage.getCreateButton().should(exist).should(be(visible)).shouldHave(text("Create"));
    }

    @Test
    @DisplayName("Should create a new person successfully")
    void shouldCreateNewPerson() {
        // Generate unique data for the test
        String uniqueEmail = "test.person." + System.currentTimeMillis() + "@example.com";
        String firstName = "John";
        String lastName = "TestPerson" + System.currentTimeMillis();

        // Create a new person
        createPerson(firstName, lastName, uniqueEmail);

        // Verify the new person appears in the table
        assertTrue(personPage.hasPersonWithEmail(uniqueEmail), "Newly created person should exist");

        // Get the row and verify the data
        SelenideElement newRow = personPage.getRowByEmail(uniqueEmail);
        assertNotNull(newRow, "New person row should not be null");
        newRow.$$("td").get(1).shouldHave(text(lastName));
        newRow.$$("td").get(2).shouldHave(text(firstName));
        newRow.$$("td").get(6).shouldHave(text(uniqueEmail));
    }

    @Test
    @DisplayName("Should view person details")
    void shouldViewPersonDetails() {
        // Create a person to view
        String uniqueEmail = "view.test." + System.currentTimeMillis() + "@example.com";
        createPerson("View", "Person", uniqueEmail);
        assertTrue(personPage.hasPersonWithEmail(uniqueEmail), "Person to be viewed should exist");

        // Get the row and its ID
        SelenideElement personRow = personPage.getRowByEmail(uniqueEmail);
        long personId = Long.parseLong(Objects.requireNonNull(personRow.getAttribute("data-person-id")));

        // Click View button
        personPage.clickViewButton(personId);

        // Verify view form loads
        personPage.getContentArea().should(exist);
        personPage.getFirstNameValue().should(exist);
        personPage.getLastNameValue().should(exist);
        personPage.getEmailValue().should(exist);

        // Verify form is in read-only mode
        assertTrue(personPage.isViewMode(), "View form should be in read-only mode");

        // Verify Back button exists
        personPage.getBackButton().should(exist).should(be(visible));
    }

    @Test
    @DisplayName("Should edit person successfully")
    void shouldEditPersonSuccessfully() {
        // Create a person to edit
        String originalEmail = "edit.test." + System.currentTimeMillis() + "@example.com";
        createPerson("Edit", "Person", originalEmail);
        assertTrue(personPage.hasPersonWithEmail(originalEmail), "Person to be edited should exist");

        // Get the row and its ID
        SelenideElement personRow = personPage.getRowByEmail(originalEmail);
        long personId = Long.parseLong(Objects.requireNonNull(personRow.getAttribute("data-person-id")));

        // Click Edit button
        personPage.clickEditButton(personId);

        // Verify edit form loads
        personPage.getContentArea().should(exist);
        personPage.getFirstNameField().should(exist);

        // Update the first name
        String updatedFirstName = "Updated" + System.currentTimeMillis();
        personPage.clearAndTypeFirstName(updatedFirstName);

        // Save the changes
        personPage.clickSaveButton();

        // Verify we're back to the list page
        personPage.getPersonTable().should(exist);

        // Verify the person was updated
        SelenideElement updatedRow = personPage.getRowById(personId);
        updatedRow.$$("td").get(2).shouldHave(text(updatedFirstName));
    }

    @Test
    @DisplayName("Should delete person successfully")
    void shouldDeletePersonSuccessfully() {
        // Create a person specifically for deletion
        String uniqueEmail = "delete.test." + System.currentTimeMillis() + "@example.com";
        createPerson("DeleteMe", "TestPerson", uniqueEmail);

        // Verify the person exists
        assertTrue(personPage.hasPersonWithEmail(uniqueEmail), "Person to be deleted should exist");

        // Get the row and its ID
        SelenideElement personRow = personPage.getRowByEmail(uniqueEmail);
        long personId = Long.parseLong(Objects.requireNonNull(personRow.getAttribute("data-person-id")));

        // Click Delete button
        personPage.clickDeleteButton(personId);

        // Verify delete confirmation dialog
        personPage.getDeleteModal().should(exist).should(be(visible));

        // Confirm deletion
        personPage.clickConfirmDeleteButton();

        // Wait for the modal to disappear and verify person is gone
        personPage.getDeleteModal().shouldNot(exist);
        personPage.getPersonTable().should(exist);
        assertFalse(personPage.hasPersonWithEmail(uniqueEmail), "Deleted person should not be in the list");
    }

    @Test
    @DisplayName("Should create person with minimal required data")
    void shouldCreatePersonWithMinimalData() {
        // Click Create button
        personPage.clickCreateButton();
        personPage.getContentArea().should(exist);

        // Fill only the required field (last name)
        String lastName = "TestLastName" + System.currentTimeMillis();
        personPage.typeLastName(lastName);

        // Save
        personPage.clickSaveButton();

        // Assert successful creation
        personPage.getPersonTable().should(exist);
        assertTrue(personPage.hasPersonWithLastName(lastName), "Person with minimal data should be created");
    }

    @Test
    @DisplayName("Should reject duplicate email on create")
    void shouldRejectDuplicateEmailOnCreate() {
        // Create first person
        String duplicateEmail = "duplicate.create." + System.currentTimeMillis() + "@example.com";
        createPerson("John", "FirstPerson", duplicateEmail);
        assertTrue(personPage.hasPersonWithEmail(duplicateEmail), "First person should exist");

        // Attempt to create second person with the same email
        personPage.clickCreateButton();
        personPage.getContentArea().should(exist);

        String firstName2 = "Jane";
        String lastName2 = "SecondPerson";
        personPage.typeFirstName(firstName2);
        personPage.typeLastName(lastName2);
        personPage.typeEmail(duplicateEmail);
        personPage.clickSaveButton();

        // Verify error message and preserved data
        personPage.getContentArea().should(exist);
        personPage.getErrorAlert().should(exist).should(be(visible)).shouldHave(anyOf(
                text("A person with this email address already exists. Please use a different email."),
                text("Another person already has this email address. Please use a different email.")
        ));
        personPage.getEmailField().shouldHave(cssClass("is-invalid"));
        personPage.getFirstNameField().shouldHave(value(firstName2));
        personPage.getLastNameField().shouldHave(value(lastName2));

        // Cancel and verify only one person exists
        personPage.clickCancelCreateButton();
        personPage.getPersonTable().should(exist);
        assertEquals(1, personPage.getRowsByEmail(duplicateEmail).size(), "Should have only one person with the duplicate email");
    }

    @Test
    @DisplayName("Should reject duplicate email on edit")
    void shouldRejectDuplicateEmailOnEdit() {
        // Create two persons
        String email1 = "person1.edit." + System.currentTimeMillis() + "@example.com";
        String email2 = "person2.edit." + System.currentTimeMillis() + "@example.com";
        createPerson("John", "Person1", email1);
        createPerson("Jane", "Person2", email2);
        assertTrue(personPage.hasPersonWithEmail(email1), "Person 1 should exist");
        assertTrue(personPage.hasPersonWithEmail(email2), "Person 2 should exist");

        // Get person 2 and try to update its email to person 1's email
        SelenideElement person2Row = personPage.getRowByEmail(email2);
        long person2Id = Long.parseLong(Objects.requireNonNull(person2Row.getAttribute("data-person-id")));

        personPage.clickEditButton(person2Id);
        personPage.getContentArea().should(exist);
        personPage.clearAndTypeEmail(email1);
        personPage.clickSaveButton();

        // Verify error message and preserved data
        personPage.getContentArea().should(exist);
        personPage.getErrorAlert().should(exist).should(be(visible)).shouldHave(anyOf(
                text("A person with this email address already exists. Please use a different email."),
                text("Another person already has this email address. Please use a different email.")
        ));
        personPage.getEmailField().shouldHave(cssClass("is-invalid")).shouldHave(value(email1));

        // Cancel and verify original data is unchanged
        personPage.clickCancelEditButton();
        personPage.getPersonTable().should(exist);

        SelenideElement updatedPerson2Row = personPage.getRowById(person2Id);
        updatedPerson2Row.$$("td").get(6).shouldHave(text(email2)); // Email should be unchanged
    }

    // Helper method to create a person
    private void createPerson(String firstName, String lastName, String email) {
        personPage.clickCreateButton();
        personPage.getContentArea().should(exist);
        personPage.typeFirstName(firstName);
        personPage.typeLastName(lastName);
        personPage.typeEmail(email);
        personPage.clickSaveButton();
        personPage.getPersonTable().should(exist);
    }
}
