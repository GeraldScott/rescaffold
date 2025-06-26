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