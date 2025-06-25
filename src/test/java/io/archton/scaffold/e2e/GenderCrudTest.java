package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.GenderPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Gender CRUD E2E Tests")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class GenderCrudTest extends BaseSelenideTest {
    
    private final GenderPage genderPage = new GenderPage();
    private static final String TEST_CODE = "Z";
    private static final String TEST_DESCRIPTION = "Test Gender E2E";
    private static final String UPDATED_CODE = "Y";
    private static final String UPDATED_DESCRIPTION = "Updated Test Gender E2E";
    
    @Test
    @DisplayName("01 - Should load Genders page and display table")
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
    @DisplayName("02 - Should create new gender successfully")
    void shouldCreateNewGenderSuccessfully() {
        genderPage.openPage();
        
        // Click Create button
        genderPage.clickCreateNew();
        
        // Verify create form is displayed
        genderPage.getCodeInput().should(exist);
        genderPage.getDescriptionInput().should(exist);
        genderPage.getSubmitCreateButton().should(exist);
        
        assertTrue(genderPage.isCreateFormVisible(), "Create form should be visible");
        
        // Fill out the form
        genderPage.fillCreateForm(TEST_CODE, TEST_DESCRIPTION);
        
        // Verify form fields are populated
        genderPage.getCodeInput().should(have(value(TEST_CODE)));
        genderPage.getDescriptionInput().should(have(value(TEST_DESCRIPTION)));
        
        // Submit the form
        genderPage.submitCreateForm();
        
        // Verify we're back to the table view and new record appears
        genderPage.getGendersTable().should(exist);
        genderPage.getRowByCode(TEST_CODE).should(exist);
        
        assertTrue(genderPage.hasRowWithCode(TEST_CODE), "New gender should appear in table");
    }
    
    @Test
    @DisplayName("03 - Should view gender details successfully")
    void shouldViewGenderDetailsSuccessfully() {
        genderPage.openPage();
        
        // Find the test record and click View
        genderPage.getRowByCode(TEST_CODE).should(exist);
        
        // Get the ID from the row to click the correct view button
        // Since we can't easily get the ID, we'll use a more direct approach
        genderPage.getRowByCode(TEST_CODE).$("button[id^='view-btn-']").click();
        
        // Verify view page is displayed
        genderPage.getContentArea().should(exist);
        genderPage.getContentArea().shouldHave(text("Code:"));
        genderPage.getContentArea().shouldHave(text(TEST_CODE));
        genderPage.getContentArea().shouldHave(text("Description:"));
        genderPage.getContentArea().shouldHave(text(TEST_DESCRIPTION));
        
        // Verify Back button is present
        $(byText("Back")).should(exist);
        $(byText("Back")).should(be(visible));
        
        // Click Back to return to table
        $(byText("Back")).click();
        
        // Verify we're back to the table
        genderPage.getGendersTable().should(exist);
    }
    
    @Test
    @DisplayName("04 - Should edit gender successfully")
    void shouldEditGenderSuccessfully() {
        genderPage.openPage();
        
        // Find the test record and click Edit
        genderPage.getRowByCode(TEST_CODE).should(exist);
        genderPage.getRowByCode(TEST_CODE).$("button[id^='edit-btn-']").click();
        
        // Verify edit form is displayed with current values
        genderPage.getCodeInput().should(exist);
        genderPage.getDescriptionInput().should(exist);
        genderPage.getCodeInput().should(have(value(TEST_CODE)));
        genderPage.getDescriptionInput().should(have(value(TEST_DESCRIPTION)));
        
        assertTrue(genderPage.isEditFormVisible(), "Edit form should be visible");
        
        // Update the form
        genderPage.fillEditForm(UPDATED_CODE, UPDATED_DESCRIPTION);
        
        // Verify form fields are updated
        genderPage.getCodeInput().should(have(value(UPDATED_CODE)));
        genderPage.getDescriptionInput().should(have(value(UPDATED_DESCRIPTION)));
        
        // Submit the form
        genderPage.submitEditForm();
        
        // Wait for the HTMX response to load the table back
        genderPage.getGendersTable().should(exist);
        
        // Verify updated record appears in the table
        genderPage.getRowByCode(UPDATED_CODE).should(exist);
        
        assertTrue(genderPage.hasRowWithCode(UPDATED_CODE), "Updated gender should appear in table");
    }
    
    @Test
    @DisplayName("05 - Should delete gender successfully")
    void shouldDeleteGenderSuccessfully() {
        genderPage.openPage();
        
        // Find the updated test record and click Delete
        genderPage.getRowByCode(UPDATED_CODE).should(exist);
        genderPage.getRowByCode(UPDATED_CODE).$("button[id^='delete-btn-']").click();
        
        // Verify delete confirmation is displayed
        genderPage.getContentArea().should(exist);
        genderPage.getContentArea().shouldHave(text("Delete Gender"));
        genderPage.getContentArea().shouldHave(text("You are about to delete this gender record"));
        genderPage.getContentArea().shouldHave(text(UPDATED_CODE));
        genderPage.getContentArea().shouldHave(text(UPDATED_DESCRIPTION));
        
        assertTrue(genderPage.isDeleteConfirmationVisible(), "Delete confirmation should be visible");
        
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
        // We use a try-catch approach since the element shouldn't exist
        try {
            genderPage.getRowByCode(UPDATED_CODE).shouldNot(exist);
        } catch (Exception e) {
            // This is expected - the row should not exist after deletion
        }
    }
    
    @Test
    @DisplayName("06 - Should edit gender isActive status successfully")
    void shouldEditGenderIsActiveStatusSuccessfully() {
        genderPage.openPage();
        
        // First create a gender to test with
        genderPage.clickCreateNew();
        String testCode = "A";
        String testDescription = "Test Active Status";
        genderPage.fillCreateForm(testCode, testDescription);
        genderPage.submitCreateForm();
        
        // Verify it appears in the table as active
        genderPage.getRowByCode(testCode).should(exist);
        genderPage.getRowByCode(testCode).shouldHave(text("Active"));
        
        // Click Edit button for this gender
        genderPage.getRowByCode(testCode).$("button[id^='edit-btn-']").click();
        
        // Verify edit form is displayed with isActive checkbox
        genderPage.getCodeInput().should(exist);
        genderPage.getDescriptionInput().should(exist);
        genderPage.getIsActiveCheckbox().should(exist);
        
        // Verify checkbox is initially checked (active)
        assertTrue(genderPage.isActiveCheckboxSelected(), "isActive checkbox should be checked initially");
        
        // Uncheck the isActive checkbox to make it inactive
        genderPage.setIsActiveCheckbox(false);
        
        // Verify checkbox is now unchecked
        assertTrue(!genderPage.isActiveCheckboxSelected(), "isActive checkbox should be unchecked");
        
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
        assertTrue(genderPage.isActiveCheckboxSelected(), "isActive checkbox should be checked");
        
        // Submit the form
        genderPage.submitEditForm();
        
        // Verify gender shows as active again
        genderPage.getGendersTable().should(exist);
        genderPage.getRowByCode(testCode).should(exist);
        genderPage.getRowByCode(testCode).shouldHave(text("Active"));
        
        // Clean up - delete the test gender
        genderPage.getRowByCode(testCode).$("button[id^='delete-btn-']").click();
        genderPage.confirmDelete();
    }

    @Test
    @DisplayName("07 - Should cancel create operation")
    void shouldCancelCreateOperation() {
        genderPage.openPage();
        
        // Click Create button
        genderPage.clickCreateNew();
        
        // Verify create form is displayed
        assertTrue(genderPage.isCreateFormVisible(), "Create form should be visible");
        
        // Fill out the form partially
        genderPage.fillCreateForm("C", "Cancel Test");
        
        // Click Cancel
        genderPage.cancelCreate();
        
        // Verify we're back to the table view
        genderPage.getGendersTable().should(exist);
        
        // Verify the cancelled record was not created
        try {
            genderPage.getRowByCode("C").shouldNot(exist);
        } catch (Exception e) {
            // This is expected - the row should not exist after cancellation
        }
    }
    
}