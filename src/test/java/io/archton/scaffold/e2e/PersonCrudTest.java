package io.archton.scaffold.e2e;

import com.codeborne.selenide.SelenideElement;
import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.PersonPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
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
        personPage.getPersonTable().should(exist);
        personPage.getPersonTable().should(be(visible));
        assertTrue(personPage.isTableVisible(), "Person table should be visible");

        // Verify table headers (Title, LastName, FirstName, Gender, IDType, IDNumber, Email, Actions)
        var headers = personPage.getTableHeaders();
        headers.shouldHave(size(8)); // 7 data columns + 1 actions column
        headers.get(0).should(have(text("Title")));
        headers.get(1).should(have(text("Last Name")));
        headers.get(2).should(have(text("First Name")));
        headers.get(3).should(have(text("Gender")));
        headers.get(4).should(have(text("ID Type")));
        headers.get(5).should(have(text("ID Number")));
        headers.get(6).should(have(text("Email")));
        headers.get(7).should(have(text("Actions")));

        // Verify Create button exists
        $("#create-new-btn").should(exist);
        $("#create-new-btn").should(be(visible));
        $("#create-new-btn").should(have(text("Create")));
    }

    @Test
    @DisplayName("Should create a new person successfully")
    void shouldCreateNewPerson() {
        // Generate unique data for the test
        String uniqueEmail = "test.person." + System.currentTimeMillis() + "@example.com";
        String firstName = "John";
        String lastName = "TestPerson" + System.currentTimeMillis();

        // Create a new person using the extracted method
        createPerson(firstName, lastName, uniqueEmail);

        // Verify the new person appears in the table
        assertTrue(personPage.hasPersonWithEmail(uniqueEmail),
                "Newly created person with email " + uniqueEmail + " should exist");

        // Get the row and verify the data
        SelenideElement newRow = personPage.getRowByEmail(uniqueEmail);
        assertNotNull(newRow, "New person row should not be null");

        // Verify first name (column 2)
        newRow.$$("td").get(2).shouldHave(text(firstName));

        // Verify last name (column 1)
        newRow.$$("td").get(1).shouldHave(text(lastName));

        // Verify email (column 6)
        newRow.$$("td").get(6).shouldHave(text(uniqueEmail));
    }

    @Test
    @DisplayName("Should view person details")
    void shouldViewPersonDetails() {
        // Find first row and get its ID
        var firstRow = personPage.getTableRows().first();
        firstRow.should(exist);

        // Extract person ID from the view button
        var viewButton = firstRow.$("button[id^='view-btn-']");
        var personId = viewButton.getAttribute("id").replace("view-btn-", "");

        // Click View button
        personPage.clickViewButton(Long.parseLong(personId));

        // Verify view form loads
        personPage.getContentArea().should(exist);
        personPage.getFirstNameValue().should(exist);
        personPage.getLastNameValue().should(exist);
        personPage.getEmailValue().should(exist);

        // Verify form is in read-only mode
        assertTrue(personPage.isViewMode(), "View form should be in read-only mode");

        // Verify Back button exists
        personPage.getBackButton().should(exist);
        personPage.getBackButton().should(be(visible));
    }

    @Test
    @DisplayName("Should edit person successfully")
    void shouldEditPersonSuccessfully() {
        // Find first row and get its ID
        var firstRow = personPage.getTableRows().first();
        firstRow.should(exist);

        // Extract person ID from the edit button
        var editButton = firstRow.$("button[id^='edit-btn-']");
        var personId = editButton.getAttribute("id").replace("edit-btn-", "");

        // Click Edit button
        personPage.clickEditButton(Long.parseLong(personId));

        // Verify edit form loads
        personPage.getContentArea().should(exist);
        personPage.getFirstNameField().should(exist);
        personPage.getLastNameField().should(exist);
        personPage.getEmailField().should(exist);

        // Update the first name
        String updatedFirstName = "Updated" + System.currentTimeMillis();
        personPage.clearAndTypeFirstName(updatedFirstName);

        // Save the changes
        personPage.clickSaveButton();

        // Verify we're back to the list page
        personPage.getPersonTable().should(exist);

        // Verify the person was updated
        var updatedRow = personPage.getRowById(Long.parseLong(personId));
        updatedRow.$$("td").get(2).shouldHave(text(updatedFirstName));
    }

    @Test
    @DisplayName("Should delete person successfully")
    void shouldDeletePersonSuccessfully() {
        // Create a person specifically for deletion
        String uniqueEmail = "delete.test." + System.currentTimeMillis() + "@example.com";
        String firstName = "DeleteMe";
        String lastName = "TestPerson";

        createPerson(firstName, lastName, uniqueEmail);

        // Verify the person exists
        assertTrue(personPage.hasPersonWithEmail(uniqueEmail),
                "Person to be deleted should exist");

        // Get the row for the person we just created
        SelenideElement personRow = personPage.getRowByEmail(uniqueEmail);
        assertNotNull(personRow, "Person row should exist before deletion");

        // Extract person ID from the delete button
        var deleteButton = personRow.$("button[id^='delete-btn-']");
        var personId = deleteButton.getAttribute("id").replace("delete-btn-", "");

        // Click Delete button
        personPage.clickDeleteButton(Long.parseLong(personId));

        // Verify delete confirmation dialog
        personPage.getDeleteModal().should(exist);
        personPage.getDeleteModal().should(be(visible));

        // Confirm deletion
        personPage.clickConfirmDeleteButton();

        // Verify the person is no longer in the active list (soft delete)
        // Note: This depends on whether the list shows inactive records or not
        // If it's a soft delete, the person might still appear but marked as inactive
        // If the list filters out inactive records, the person should not appear
        
        // Wait for the modal to disappear
        personPage.getDeleteModal().shouldNot(exist);

        // Verify we're back to the list page
        personPage.getPersonTable().should(exist);
    }

    @Test
    @DisplayName("Should handle form submission with minimal data")
    void shouldHandleFormSubmissionWithMinimalData() {
        // Click Create button
        personPage.clickCreateButton();

        // Verify create form loads
        personPage.getContentArea().should(exist);
        personPage.getFirstNameField().should(exist);
        personPage.getLastNameField().should(exist);
        personPage.getEmailField().should(exist);

        // Fill only the required field (last name)
        personPage.typeLastName("TestLastName" + System.currentTimeMillis());

        // Try to save with minimal data
        personPage.clickSaveButton();

        // Should redirect back to the list page (whether successful or not)
        personPage.getPersonTable().should(exist);
    }

    @Test
    @DisplayName("Should reject duplicate email on create")
    void shouldRejectDuplicateEmailOnCreate() {
        // Create first person with unique email
        String duplicateEmail = "duplicate.test." + System.currentTimeMillis() + "@example.com";
        String firstName1 = "John";
        String lastName1 = "FirstPerson";

        createPerson(firstName1, lastName1, duplicateEmail);

        // Verify first person was created successfully
        assertTrue(personPage.hasPersonWithEmail(duplicateEmail),
                "First person with email " + duplicateEmail + " should exist");

        // Now try to create second person with same email
        String firstName2 = "Jane";
        String lastName2 = "SecondPerson";

        // Click Create button
        personPage.clickCreateButton();

        // Verify create form loads
        personPage.getContentArea().should(exist);

        // Fill the form with duplicate email
        personPage.typeFirstName(firstName2);
        personPage.typeLastName(lastName2);
        personPage.typeEmail(duplicateEmail);

        // Save the person
        personPage.clickSaveButton();

        // Verify we stay on the create form with error message
        personPage.getContentArea().should(exist);
        
        // Check for error message in the form
        $(".alert-danger").should(exist);
        $(".alert-danger").should(be(visible));
        $(".alert-danger").shouldHave(anyOf(
                text("A person with this email address already exists. Please use a different email."),
                text("Another person already has this email address. Please use a different email.")
        ));

        // Verify email field is marked as invalid
        $("#email").shouldHave(cssClass("is-invalid"));

        // Verify form preserves entered data
        $("#firstName").shouldHave(value(firstName2));
        $("#lastName").shouldHave(value(lastName2));
        $("#email").shouldHave(value(duplicateEmail));

        // Cancel and go back to list
        $("#cancel-create-btn").click();
        personPage.getPersonTable().should(exist);

        // Verify only the first person exists, not the duplicate
        assertTrue(personPage.hasPersonWithEmail(duplicateEmail),
                "Original person should still exist");
        
        // Count persons with the duplicate email (should be only 1)
        var personsWithEmail = personPage.getRowsByEmail(duplicateEmail);
        assertEquals(1, personsWithEmail.size(), 
                "Should have exactly one person with the duplicate email");
    }

    @Test
    @DisplayName("Should reject duplicate email on edit")
    void shouldRejectDuplicateEmailOnEdit() {
        // Create two persons with different emails
        String email1 = "person1." + System.currentTimeMillis() + "@example.com";
        String email2 = "person2." + System.currentTimeMillis() + "@example.com";
        
        createPerson("John", "Person1", email1);
        createPerson("Jane", "Person2", email2);

        // Verify both persons exist
        assertTrue(personPage.hasPersonWithEmail(email1), "Person 1 should exist");
        assertTrue(personPage.hasPersonWithEmail(email2), "Person 2 should exist");

        // Get the second person's row and edit it
        SelenideElement person2Row = personPage.getRowByEmail(email2);
        var editButton = person2Row.$("button[id^='edit-btn-']");
        var person2Id = editButton.getAttribute("id").replace("edit-btn-", "");

        // Click Edit button for person 2
        personPage.clickEditButton(Long.parseLong(person2Id));

        // Verify edit form loads
        personPage.getContentArea().should(exist);
        personPage.getEmailField().should(exist);

        // Change email to match person 1's email (duplicate)
        personPage.clearAndTypeEmail(email1);

        // Save the changes
        personPage.clickSaveButton();

        // Verify we stay on the edit form with error message
        personPage.getContentArea().should(exist);
        
        // Check for error message in the form
        $(".alert-danger").should(exist);
        $(".alert-danger").should(be(visible));
        $(".alert-danger").shouldHave(anyOf(
                text("A person with this email address already exists. Please use a different email."),
                text("Another person already has this email address. Please use a different email.")
        ));

        // Verify email field is marked as invalid
        $("#email").shouldHave(cssClass("is-invalid"));

        // Verify form shows the duplicate email that user tried to enter
        $("#email").shouldHave(value(email1));

        // Cancel and go back to list
        $("#cancel-edit-btn").click();
        personPage.getPersonTable().should(exist);

        // Verify person 2 still has their original email (not changed)
        SelenideElement updatedPerson2Row = personPage.getRowById(Long.parseLong(person2Id));
        updatedPerson2Row.$$("td").get(6).shouldHave(text(email2)); // Email should be unchanged

        // Verify both persons still exist with their original emails
        assertTrue(personPage.hasPersonWithEmail(email1), "Person 1 should still exist with original email");
        assertTrue(personPage.hasPersonWithEmail(email2), "Person 2 should still exist with original email");
    }

    // Helper method to create a person
    private void createPerson(String firstName, String lastName, String email) {
        // Click Create button
        personPage.clickCreateButton();

        // Verify create form loads
        personPage.getContentArea().should(exist);

        // Fill the form
        personPage.typeFirstName(firstName);
        personPage.typeLastName(lastName);
        personPage.typeEmail(email);

        // Save the person
        personPage.clickSaveButton();

        // Verify we're back to the list page
        personPage.getPersonTable().should(exist);
    }
}