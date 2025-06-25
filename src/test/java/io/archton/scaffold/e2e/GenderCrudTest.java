package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.GenderPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Gender CRUD E2E Tests")
class GenderCrudTest extends BaseSelenideTest {
    
    private final GenderPage genderPage = new GenderPage();
    private String testCode;
    private String testDescription;
    private String updatedCode;
    private String updatedDescription;
    
    @BeforeEach
    void setUpTestData() {
        // Generate single character codes for Gender entity (maxlength=1)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ss"));
        char testChar = (char) ('A' + (Integer.parseInt(timestamp) % 26));
        char updatedChar = (char) ('A' + ((Integer.parseInt(timestamp) + 1) % 26));
        
        testCode = String.valueOf(testChar);
        testDescription = "Test Gender " + testChar;
        updatedCode = String.valueOf(updatedChar);
        updatedDescription = "Updated Test Gender " + updatedChar;
    }
    
    @AfterEach
    void cleanUpTestData() {
        // Clean up any test data that might have been created
        genderPage.openPage();
        try {
            if (genderPage.hasRowWithCode(testCode)) {
                genderPage.getRowByCode(testCode).$("button[id^='delete-btn-']").click();
                genderPage.confirmDelete();
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        try {
            if (genderPage.hasRowWithCode(updatedCode)) {
                genderPage.getRowByCode(updatedCode).$("button[id^='delete-btn-']").click();
                genderPage.confirmDelete();
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    @DisplayName("Should load Genders page and display table")
    void shouldLoadGendersPageAndDisplayTable() {
        genderPage.openPage();
        
        // Verify page loads successfully
        genderPage.getPageTitle().should(exist);
        genderPage.getPageTitle().should(have(text("Genders")));
        
        // Verify content area and table are visible
        genderPage.getContentArea().should(exist);
        genderPage.getContentArea().should(be(visible));
        
        genderPage.getGendersTable().should(exist);
        genderPage.getGendersTable().should(be(visible));
        
        assertTrue(genderPage.isTableVisible(), "Genders table should be visible");
        assertTrue(genderPage.isContentAreaVisible(), "Content area should be visible");
        
        // Verify Create button is present
        genderPage.getCreateNewButton().should(exist);
        genderPage.getCreateNewButton().should(be(visible));
        genderPage.getCreateNewButton().should(have(text("Create")));
    }
    
    @Test
    @DisplayName("Should create new gender successfully")
    void shouldCreateNewGenderSuccessfully() {
        genderPage.openPage();
        
        // Click Create button
        genderPage.clickCreateNew();
        
        // Verify create form is displayed
        genderPage.getCodeInput().should(exist);
        genderPage.getDescriptionInput().should(exist);
        genderPage.getSubmitCreateButton().should(exist);
        
        // Fill out the form
        genderPage.fillCreateForm(testCode, testDescription);
        
        // Verify form fields are populated
        genderPage.getCodeInput().should(have(value(testCode)));
        genderPage.getDescriptionInput().should(have(value(testDescription)));
        
        // Submit the form
        genderPage.submitCreateForm();
        
        // Wait for HTMX to complete and verify we're back to the table view and new record appears
        genderPage.getGendersTable().should(exist);
        genderPage.getRowByCode(testCode).should(exist);
    }
    
    @Test
    @DisplayName("Should view gender details successfully")
    void shouldViewGenderDetailsSuccessfully() {
        // First create a gender to view
        genderPage.openPage();
        genderPage.clickCreateNew();
        genderPage.fillCreateForm(testCode, testDescription);
        genderPage.submitCreateForm();
        
        // Wait for table to load after creation, then find the test record and click View
        genderPage.getGendersTable().should(exist);
        genderPage.getRowByCode(testCode).should(exist);
        genderPage.getRowByCode(testCode).$("button[id^='view-btn-']").click();
        
        // Verify view page is displayed
        genderPage.getContentArea().should(exist);
        genderPage.getContentArea().shouldHave(text("Code:"));
        genderPage.getContentArea().shouldHave(text(testCode));
        genderPage.getContentArea().shouldHave(text("Description:"));
        genderPage.getContentArea().shouldHave(text(testDescription));
        
        // Verify Back button is present
        $(byText("Back")).should(exist);
        $(byText("Back")).should(be(visible));
        
        // Click Back to return to table
        $(byText("Back")).click();
        
        // Verify we're back to the table
        genderPage.getGendersTable().should(exist);
    }
    
    @Test
    @DisplayName("Should edit gender successfully")
    void shouldEditGenderSuccessfully() {
        // First create a gender to edit
        genderPage.openPage();
        genderPage.clickCreateNew();
        genderPage.fillCreateForm(testCode, testDescription);
        genderPage.submitCreateForm();
        
        // Wait for table to load after creation, then find the test record and click Edit
        genderPage.getGendersTable().should(exist);
        genderPage.getRowByCode(testCode).should(exist);
        genderPage.getRowByCode(testCode).$("button[id^='edit-btn-']").click();
        
        // Verify edit form is displayed with current values
        genderPage.getCodeInput().should(exist);
        genderPage.getDescriptionInput().should(exist);
        genderPage.getCodeInput().should(have(value(testCode)));
        genderPage.getDescriptionInput().should(have(value(testDescription)));
        
        // Update the form
        genderPage.fillEditForm(updatedCode, updatedDescription);
        
        // Verify form fields are updated
        genderPage.getCodeInput().should(have(value(updatedCode)));
        genderPage.getDescriptionInput().should(have(value(updatedDescription)));
        
        // Submit the form
        genderPage.submitEditForm();
        
        // Wait for the HTMX response to load the table back
        genderPage.getGendersTable().should(exist);
        
        // Verify updated record appears in the table
        genderPage.getRowByCode(updatedCode).should(exist);
    }
    
    @Test
    @DisplayName("Should delete gender successfully")
    void shouldDeleteGenderSuccessfully() {
        // First create a gender to delete
        genderPage.openPage();
        genderPage.clickCreateNew();
        genderPage.fillCreateForm(testCode, testDescription);
        genderPage.submitCreateForm();
        
        // Wait for table to load after creation, then find the test record and click Delete
        genderPage.getGendersTable().should(exist);
        genderPage.getRowByCode(testCode).should(exist);
        genderPage.getRowByCode(testCode).$("button[id^='delete-btn-']").click();
        
        // Verify delete confirmation is displayed
        genderPage.getContentArea().should(exist);
        genderPage.getContentArea().shouldHave(text("Delete Gender"));
        genderPage.getContentArea().shouldHave(text("You are about to delete this gender record"));
        genderPage.getContentArea().shouldHave(text(testCode));
        genderPage.getContentArea().shouldHave(text(testDescription));
        
        // Verify Cancel and Delete buttons are present
        genderPage.getConfirmDeleteButton().should(exist);
        genderPage.getConfirmDeleteButton().should(be(visible));
        genderPage.getConfirmDeleteButton().should(have(text("Delete")));
        
        $(byText("Cancel")).should(exist);
        $(byText("Cancel")).should(be(visible));
        
        // Confirm deletion
        genderPage.confirmDelete();
        
        // Verify we're back to the table view and record is gone
        genderPage.getGendersTable().should(exist);
        
        // Verify the record no longer exists in the table
        genderPage.getRowByCode(testCode).shouldNot(exist);
    }
    
    @Test
    @DisplayName("Should edit gender isActive status successfully")
    void shouldEditGenderIsActiveStatusSuccessfully() {
        genderPage.openPage();
        
        // First create a gender to test with
        genderPage.clickCreateNew();
        genderPage.fillCreateForm(testCode, testDescription);
        genderPage.submitCreateForm();
        
        // Wait for table to load after creation, then verify it appears as active
        genderPage.getGendersTable().should(exist);
        genderPage.getRowByCode(testCode).should(exist);
        genderPage.getRowByCode(testCode).shouldHave(text("Active"));
        
        // Click Edit button for this gender
        genderPage.getRowByCode(testCode).$("button[id^='edit-btn-']").click();
        
        // Verify edit form is displayed with isActive checkbox
        genderPage.getCodeInput().should(exist);
        genderPage.getDescriptionInput().should(exist);
        genderPage.getIsActiveCheckbox().should(exist);
        
        // Verify checkbox is initially checked (active)
        genderPage.getIsActiveCheckbox().should(be(checked));
        
        // Uncheck the isActive checkbox to make it inactive
        genderPage.setIsActiveCheckbox(false);
        
        // Verify checkbox is now unchecked
        genderPage.getIsActiveCheckbox().shouldNot(be(checked));
        
        // Submit the form
        genderPage.submitEditForm();
        
        // Verify we're back to the table and gender shows as inactive
        genderPage.getGendersTable().should(exist);
        genderPage.getRowByCode(testCode).should(exist);
        genderPage.getRowByCode(testCode).shouldHave(text("Inactive"));
        
        // Edit again to make it active
        genderPage.getRowByCode(testCode).$("button[id^='edit-btn-']").click();
        
        // Check the isActive checkbox to make it active again
        genderPage.setIsActiveCheckbox(true);
        
        // Verify checkbox is now checked
        genderPage.getIsActiveCheckbox().should(be(checked));
        
        // Submit the form
        genderPage.submitEditForm();
        
        // Verify gender shows as active again
        genderPage.getGendersTable().should(exist);
        genderPage.getRowByCode(testCode).should(exist);
        genderPage.getRowByCode(testCode).shouldHave(text("Active"));
    }

    @Test
    @DisplayName("Should cancel create operation")
    void shouldCancelCreateOperation() {
        genderPage.openPage();
        
        // Click Create button
        genderPage.clickCreateNew();
        
        // Verify create form is displayed
        genderPage.getCodeInput().should(exist);
        genderPage.getDescriptionInput().should(exist);
        
        // Fill out the form partially
        String cancelCode = "CANCEL" + System.currentTimeMillis();
        genderPage.fillCreateForm(cancelCode, "Cancel Test");
        
        // Click Cancel
        genderPage.cancelCreate();
        
        // Verify we're back to the table view
        genderPage.getGendersTable().should(exist);
        
        // Verify the cancelled record was not created
        genderPage.getRowByCode(cancelCode).shouldNot(exist);
    }
    
}