package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.PersonPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.$;

@DisplayName("Person CRUD E2E Tests")
class PersonCrudTest extends BaseSelenideTest {
    
    private final PersonPage personPage = new PersonPage();
    
    @Test
    @DisplayName("Should display person table when page loads")
    void shouldDisplayPersonTableWhenPageLoads() {
        personPage.openPage();
        
        personPage.getPageTitle().should(exist);
        personPage.getPageTitle().should(be(visible));
        personPage.getPageTitle().should(have(text("People")));
        
        personPage.getPersonTable().should(exist);
        personPage.getPersonTable().should(be(visible));
        
        personPage.getTableHeaders().shouldHave(texts("TITLE", "LAST NAME", "FIRST NAME", "GENDER", "ID TYPE", "ID NUMBER", "EMAIL", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        personPage.openPage();
        
        personPage.getCreateButton().should(exist);
        personPage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        personPage.openPage();
        
        personPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        int rowCount = personPage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            personPage.getViewButton(i).should(exist);
            personPage.getViewButton(i).should(be(visible));
            
            personPage.getEditButton(i).should(exist);
            personPage.getEditButton(i).should(be(visible));
            
            personPage.getDeleteButton(i).should(exist);
            personPage.getDeleteButton(i).should(be(visible));
        }
    }
    
    @Test
    @DisplayName("Should open Create screen when Create button is clicked")
    void shouldOpenCreateScreenWhenCreateButtonIsClicked() {
        personPage.openPage();
        
        personPage.getCreateButton().should(exist);
        personPage.getCreateButton().should(be(visible));
        
        personPage.clickCreate();
        
        personPage.getFirstNameInput().should(exist);
        personPage.getFirstNameInput().should(be(visible));
        
        personPage.getLastNameInput().should(exist);
        personPage.getLastNameInput().should(be(visible));
        
        personPage.getEmailInput().should(exist);
        personPage.getEmailInput().should(be(visible));
        
        personPage.getSaveButton().should(exist);
        personPage.getSaveButton().should(be(visible));
        
        personPage.getCancelButton().should(exist);
        personPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open View screen when View button is clicked")
    void shouldOpenViewScreenWhenViewButtonIsClicked() {
        personPage.openPage();
        
        personPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        personPage.getViewButton(0).should(exist);
        personPage.getViewButton(0).should(be(visible));
        personPage.getViewButton(0).click();
        
        personPage.getDetailCard().should(exist);
        personPage.getDetailCard().should(be(visible));
        
        personPage.getBackButton().should(exist);
        personPage.getBackButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open Edit screen when Edit button is clicked")
    void shouldOpenEditScreenWhenEditButtonIsClicked() {
        personPage.openPage();
        
        personPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        personPage.getEditButton(0).should(exist);
        personPage.getEditButton(0).should(be(visible));
        personPage.getEditButton(0).click();
        
        personPage.getFirstNameInput().should(exist);
        personPage.getFirstNameInput().should(be(visible));
        
        personPage.getLastNameInput().should(exist);
        personPage.getLastNameInput().should(be(visible));
        
        personPage.getEmailInput().should(exist);
        personPage.getEmailInput().should(be(visible));
        
        personPage.getSaveButton().should(exist);
        personPage.getSaveButton().should(be(visible));
        
        personPage.getCancelButton().should(exist);
        personPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should respond when Delete button is clicked")
    void shouldRespondWhenDeleteButtonIsClicked() {
        personPage.openPage();
        
        personPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        personPage.getDeleteButton(0).should(exist);
        personPage.getDeleteButton(0).should(be(visible));
        personPage.getDeleteButton(0).click();
        
        // After clicking delete, we should see the delete confirmation screen
        // Wait for HTMX updates to complete and verify the delete confirmation elements appear
        $("#confirm-delete-btn").should(appear, Duration.ofSeconds(3));
        $("#cancel-delete-btn").should(appear, Duration.ofSeconds(3));
        
        // Verify warning message appears (Person uses alert-danger instead of alert-warning)
        $(".alert-danger").should(appear);
        
        // Check if we're still on a page with content
        // This verifies the delete button triggered the expected action (showing confirmation)
        $("body").should(exist);
    }
    
    @Test
    @DisplayName("Should reject invalid RSA ID number when creating person with ID type")
    void shouldRejectInvalidRsaIdNumberOnCreate() {
        personPage.openPage();
        personPage.clickCreate();
        
        // Fill form with invalid RSA ID
        personPage.getFirstNameInput().setValue("John");
        personPage.getLastNameInput().setValue("Doe");
        personPage.getEmailInput().setValue("john.invalid.rsa@example.com");
        
        // Select ID type from dropdown
        personPage.getIdTypeSelect().selectOption("National identity document");
        personPage.getIdNumberInput().setValue("1234567890123"); // Invalid RSA ID
        
        personPage.clickSave();
        
        // Should show validation error
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        $(".alert-danger").should(have(text("Invalid RSA ID number format or checksum")));
    }
    
    @Test
    @DisplayName("Should accept valid RSA ID number when creating person with ID type")
    void shouldAcceptValidRsaIdNumberOnCreate() {
        personPage.openPage();
        personPage.clickCreate();
        
        // Fill form with valid RSA ID
        personPage.getFirstNameInput().setValue("Jane");
        personPage.getLastNameInput().setValue("Smith");
        personPage.getEmailInput().setValue("jane.valid.rsa@example.com");
        
        // Select ID type from dropdown
        personPage.getIdTypeSelect().selectOption("National identity document");
        personPage.getIdNumberInput().setValue("8001015009087"); // Valid RSA ID
        
        personPage.clickSave();
        
        // Should successfully create and redirect to list
        personPage.getPersonTable().should(appear, Duration.ofSeconds(3));
        
        // Verify new person appears in table
        personPage.getPersonTable()
                .shouldHave(text("Jane"))
                .shouldHave(text("Smith"))
                .shouldHave(text("8001015009087"));
    }
    
    @Test
    @DisplayName("Should accept any ID number format for non-ID types")
    void shouldAcceptAnyIdNumberForNonIdTypes() {
        personPage.openPage();
        personPage.clickCreate();
        
        // Fill form with passport type
        personPage.getFirstNameInput().setValue("Bob");
        personPage.getLastNameInput().setValue("Wilson");
        personPage.getEmailInput().setValue("bob.passport@example.com");
        
        // Select PASSPORT type from dropdown (assuming it exists)
        personPage.getIdTypeSelect().selectOption("Passport");
        personPage.getIdNumberInput().setValue("ABC123456"); // Any format allowed
        
        personPage.clickSave();
        
        // Should successfully create
        personPage.getPersonTable().should(appear, Duration.ofSeconds(3));
        
        // Verify new person appears in table
        personPage.getPersonTable()
                .shouldHave(text("Bob"))
                .shouldHave(text("Wilson"))
                .shouldHave(text("ABC123456"));
    }
}