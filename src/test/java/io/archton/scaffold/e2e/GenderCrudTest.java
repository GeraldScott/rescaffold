package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.GenderPage;
import io.archton.scaffold.e2e.pages.HomePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Gender CRUD E2E Tests")
class GenderCrudTest extends BaseSelenideTest {
    
    private final HomePage homePage = new HomePage();
    private final GenderPage genderPage = new GenderPage();
    
    @Test
    @DisplayName("Should navigate to Genders page from Maintenance menu")
    void shouldNavigateToGendersPage() {
        // Navigate to home page
        homePage.openPage();
        
        // Click Maintenance dropdown
        homePage.clickMaintenanceDropdown();
        
        // Verify Genders link is visible
        homePage.getGendersDropdownLink().should(exist);
        homePage.getGendersDropdownLink().should(be(visible));
        homePage.getGendersDropdownLink().should(have(text("Genders")));
        
        // Click Genders link
        homePage.clickGendersLink();
        
        // Verify navigation to Genders page
        webdriver().shouldHave(url(BASE_URL + "/genders-ui"));
        
        // Verify page loads correctly
        genderPage.getPageTitle().should(have(text("Genders")));
        genderPage.getContentArea().should(exist);
        genderPage.getGenderTable().should(exist);
    }
    
    @Test
    @DisplayName("Should display gender list table with correct headers")
    void shouldDisplayGenderListTable() {
        // Navigate directly to genders page
        genderPage.openPage();
        
        // Verify table exists and is visible
        genderPage.getGenderTable().should(exist);
        genderPage.getGenderTable().should(be(visible));
        assertTrue(genderPage.isTableVisible(), "Gender table should be visible");
        
        // Verify table headers
        var headers = genderPage.getTableHeaders();
        headers.shouldHave(size(8));
        headers.get(0).should(have(text("Code")));
        headers.get(1).should(have(text("Description")));
        headers.get(2).should(have(text("Created By")));
        headers.get(3).should(have(text("Created At")));
        headers.get(4).should(have(text("Updated By")));
        headers.get(5).should(have(text("Updated At")));
        headers.get(6).should(have(text("Active")));
        headers.get(7).should(have(text("Actions")));
        
        // Verify Create button exists
        $("#create-new-btn").should(exist);
        $("#create-new-btn").should(be(visible));
        $("#create-new-btn").should(have(text("Create")));
    }
    
    @Test
    @DisplayName("Should view gender details")
    void shouldViewGenderDetails() {
        // Navigate to genders page
        genderPage.openPage();
        
        // Wait for table to load
        genderPage.getGenderTable().should(exist);
        
        // Find first row and get its ID
        var firstRow = genderPage.getTableRows().first();
        firstRow.should(exist);
        
        // Extract gender ID from the view button
        var viewButton = firstRow.$("button[id^='view-btn-']");
        var genderId = viewButton.getAttribute("id").replace("view-btn-", "");
        
        // Click View button
        genderPage.clickViewButton(Long.parseLong(genderId));
        
        // Verify view form loads
        genderPage.getContentArea().should(exist);
        genderPage.getCodeValue().should(exist);
        genderPage.getCodeValue().should(be(visible));
        
        // Verify Back button exists
        $("button:has(.bi-arrow-left)").should(exist);
        $("button:has(.bi-arrow-left)").should(have(text("Back")));
        
        // Click Back button
        genderPage.clickBackButton();
        
        // Verify return to list
        genderPage.getGenderTable().should(exist);
        genderPage.getPageTitle().should(have(text("Genders")));
    }
    
    @Test
    @DisplayName("Should create new gender successfully")
    void shouldCreateNewGenderSuccessfully() {
        // Navigate to genders page
        genderPage.openPage();
        
        // Click Create button
        genderPage.clickCreateButton();
        
        // Verify create form loads
        genderPage.getContentArea().should(exist);
        $(".card-header").should(have(text("Create New Gender")));
        
        // Fill in form with valid data
        // Generate a random uppercase letter between P-Z to avoid conflicts
        char randomChar = (char) ('P' + (int)(Math.random() * 11)); // P to Z
        String uniqueCode = String.valueOf(randomChar);
        String uniqueDescription = "Test Gender " + System.currentTimeMillis();
        
        genderPage.enterCode(uniqueCode);
        genderPage.enterDescription(uniqueDescription);
        
        // Submit form
        genderPage.clickSubmitCreateButton();
        
        // Check if we got an error (duplicate code)
        if ($(".alert-danger").exists()) {
            // If code already exists, try a different one
            char newRandomChar = (char) ('P' + (int)(Math.random() * 11));
            uniqueCode = String.valueOf(newRandomChar);
            genderPage.getCodeInput().clear();
            genderPage.enterCode(uniqueCode);
            genderPage.clickSubmitCreateButton();
        }
        
        // Verify return to list
        genderPage.getGenderTable().should(exist);
        
        // Verify new gender appears in table
        assertTrue(genderPage.hasGenderWithCode(uniqueCode), "New gender should appear in table");
        var newRow = genderPage.getRowByCode(uniqueCode);
        newRow.$("td", 1).should(have(text(uniqueDescription)));
        newRow.$("td .badge.bg-success").should(have(text("Active")));
    }
    
    @Test
    @DisplayName("Should validate required fields on create")
    void shouldValidateRequiredFieldsOnCreate() {
        // Navigate to genders page
        genderPage.openPage();
        
        // Click Create button
        genderPage.clickCreateButton();
        
        // Try to submit empty form
        genderPage.clickSubmitCreateButton();
        
        // Browser should prevent submission due to required fields
        // Form should still be visible
        $(".card-header").should(have(text("Create New Gender")));
        genderPage.getCodeInput().should(exist);
        
        // Enter only code and try to submit
        genderPage.enterCode("Y");
        genderPage.getDescriptionInput().clear();
        genderPage.clickSubmitCreateButton();
        
        // Form should still be visible (description is required)
        $(".card-header").should(have(text("Create New Gender")));
    }
    
    @Test
    @DisplayName("Should validate code format")
    void shouldValidateCodeFormat() {
        // Navigate to genders page
        genderPage.openPage();
        
        // Click Create button
        genderPage.clickCreateButton();
        
        // Try lowercase code
        genderPage.enterCode("a");
        genderPage.enterDescription("Test lowercase");
        
        // Browser pattern validation should prevent submission
        genderPage.clickSubmitCreateButton();
        
        // Form should still be visible
        $(".card-header").should(have(text("Create New Gender")));
        
        // Try number
        genderPage.getCodeInput().clear();
        genderPage.enterCode("1");
        
        // Browser pattern validation should prevent submission
        genderPage.clickSubmitCreateButton();
        
        // Form should still be visible
        $(".card-header").should(have(text("Create New Gender")));
    }
    
    @Test
    @DisplayName("Should edit existing gender")
    void shouldEditExistingGender() {
        // Navigate to genders page
        genderPage.openPage();
        
        // Wait for table to load
        genderPage.getGenderTable().should(exist);
        
        // Find first row and get its ID
        var firstRow = genderPage.getTableRows().first();
        firstRow.should(exist);
        
        // Get original description
        var originalDescription = firstRow.$("td", 1).getText();
        
        // Extract gender ID from edit button
        var editButton = firstRow.$("button[id^='edit-btn-']");
        var genderId = editButton.getAttribute("id").replace("edit-btn-", "");
        
        // Click Edit button
        genderPage.clickEditButton(Long.parseLong(genderId));
        
        // Verify edit form loads
        genderPage.getContentArea().should(exist);
        $(".card-header").should(have(text("Edit Gender")));
        
        // Update description
        String updatedDescription = originalDescription + " Updated " + System.currentTimeMillis();
        genderPage.getDescriptionInput().clear();
        genderPage.enterDescription(updatedDescription);
        
        // Submit form
        $("#submit-edit-btn").click();
        
        // Verify return to list
        genderPage.getGenderTable().should(exist);
        
        // Verify updated description appears
        firstRow = genderPage.getTableRows().first();
        firstRow.$("td", 1).should(have(text(updatedDescription)));
    }
    
    @Test
    @DisplayName("Should delete gender")
    void shouldDeleteGender() {
        // First create a gender to delete
        genderPage.openPage();
        genderPage.clickCreateButton();
        
        String deleteCode = "W";
        String deleteDescription = "To Be Deleted " + System.currentTimeMillis();
        
        genderPage.enterCode(deleteCode);
        genderPage.enterDescription(deleteDescription);
        genderPage.clickSubmitCreateButton();
        
        // Wait for list to reload
        genderPage.getGenderTable().should(exist);
        
        // Find the created gender
        var rowToDelete = genderPage.getRowByCode(deleteCode);
        rowToDelete.should(exist);
        
        // Extract gender ID from delete button
        var deleteButton = rowToDelete.$("button[id^='delete-btn-']");
        var genderId = deleteButton.getAttribute("id").replace("delete-btn-", "");
        
        // Click Delete button
        genderPage.clickDeleteButton(Long.parseLong(genderId));
        
        // Verify delete confirmation form loads
        genderPage.getContentArea().should(exist);
        $(".card-header").should(have(text("Delete Gender")));
        $(".alert-warning").should(exist);
        
        // Confirm deletion
        genderPage.clickConfirmDeleteButton();
        
        // Verify return to list
        genderPage.getGenderTable().should(exist);
        
        // Verify gender is removed from table
        assertFalse(genderPage.hasGenderWithCode(deleteCode), "Deleted gender should not appear in table");
    }
    
    @Test
    @DisplayName("Should cancel delete operation")
    void shouldCancelDeleteOperation() {
        // Navigate to genders page
        genderPage.openPage();
        
        // Wait for table to load
        genderPage.getGenderTable().should(exist);
        
        // Find first row
        var firstRow = genderPage.getTableRows().first();
        firstRow.should(exist);
        
        // Get the code to verify later
        var genderCode = firstRow.$("td.fw-bold").getText();
        
        // Extract gender ID from delete button
        var deleteButton = firstRow.$("button[id^='delete-btn-']");
        var genderId = deleteButton.getAttribute("id").replace("delete-btn-", "");
        
        // Click Delete button
        genderPage.clickDeleteButton(Long.parseLong(genderId));
        
        // Verify delete confirmation form loads
        $(".card-header").should(have(text("Delete Gender")));
        
        // Cancel deletion
        genderPage.clickCancelDeleteButton();
        
        // Verify return to list
        genderPage.getGenderTable().should(exist);
        
        // Verify gender still exists
        assertTrue(genderPage.hasGenderWithCode(genderCode), "Gender should still exist after cancelled delete");
    }
}