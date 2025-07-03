package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.GenderPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.$;

@DisplayName("Gender CRUD E2E Tests")
class GenderCrudTest extends BaseSelenideTest {
    
    private final GenderPage genderPage = new GenderPage();
    
    @Test
    @DisplayName("Should display gender table when page loads")
    void shouldDisplayGenderTableWhenPageLoads() {
        // Open the gender page directly
        genderPage.openPage();
        
        // Verify page title
        genderPage.getPageTitle().should(exist);
        genderPage.getPageTitle().should(be(visible));
        genderPage.getPageTitle().should(have(text("Genders")));
        
        // Verify gender table exists and is visible
        genderPage.getGenderTable().should(exist);
        genderPage.getGenderTable().should(be(visible));
        
        // Verify table headers exist and contain expected text
        genderPage.getTableHeaders().shouldHave(texts("CODE", "DESCRIPTION", "CREATED BY", "CREATED AT", "UPDATED BY", "UPDATED AT", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        // Open the gender page
        genderPage.openPage();
        
        // Verify Create button exists and is visible
        genderPage.getCreateButton().should(exist);
        genderPage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        // Open the gender page
        genderPage.openPage();
        
        // Verify table has at least one row (assuming there's test data)
        genderPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // For each row, verify View, Edit, and Delete buttons exist
        int rowCount = genderPage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            // Verify View button exists and is visible
            genderPage.getViewButton(i).should(exist);
            genderPage.getViewButton(i).should(be(visible));
            
            // Verify Edit button exists and is visible
            genderPage.getEditButton(i).should(exist);
            genderPage.getEditButton(i).should(be(visible));
            
            // Verify Delete button exists and is visible
            genderPage.getDeleteButton(i).should(exist);
            genderPage.getDeleteButton(i).should(be(visible));
        }
    }
    
    @Test
    @DisplayName("Should open Create screen when Create button is clicked")
    void shouldOpenCreateScreenWhenCreateButtonIsClicked() {
        // Open the gender page
        genderPage.openPage();
        
        // Verify Create button exists
        genderPage.getCreateButton().should(exist);
        genderPage.getCreateButton().should(be(visible));
        
        // Click the Create button
        genderPage.clickCreate();
        
        // Verify the create form elements are now visible
        genderPage.getCodeInput().should(exist);
        genderPage.getCodeInput().should(be(visible));
        
        genderPage.getDescriptionInput().should(exist);
        genderPage.getDescriptionInput().should(be(visible));
        
        genderPage.getSaveButton().should(exist);
        genderPage.getSaveButton().should(be(visible));
        
        genderPage.getCancelButton().should(exist);
        genderPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open View screen when View button is clicked")
    void shouldOpenViewScreenWhenViewButtonIsClicked() {
        // Open the gender page
        genderPage.openPage();
        
        // Verify table has at least one row
        genderPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Click the View button for the first row
        genderPage.getViewButton(0).should(exist);
        genderPage.getViewButton(0).should(be(visible));
        genderPage.getViewButton(0).click();
        
        // Verify the detail view elements are now visible
        genderPage.getDetailCard().should(exist);
        genderPage.getDetailCard().should(be(visible));
        
        genderPage.getBackButton().should(exist);
        genderPage.getBackButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open Edit screen when Edit button is clicked")
    void shouldOpenEditScreenWhenEditButtonIsClicked() {
        // Open the gender page
        genderPage.openPage();
        
        // Verify table has at least one row
        genderPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Click the Edit button for the first row
        genderPage.getEditButton(0).should(exist);
        genderPage.getEditButton(0).should(be(visible));
        genderPage.getEditButton(0).click();
        
        // Verify the edit form elements are now visible
        genderPage.getCodeInput().should(exist);
        genderPage.getCodeInput().should(be(visible));
        
        genderPage.getDescriptionInput().should(exist);
        genderPage.getDescriptionInput().should(be(visible));
        
        genderPage.getSaveButton().should(exist);
        genderPage.getSaveButton().should(be(visible));
        
        genderPage.getCancelButton().should(exist);
        genderPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should respond when Delete button is clicked")
    void shouldRespondWhenDeleteButtonIsClicked() {
        // Open the gender page
        genderPage.openPage();
        
        // Verify table has at least one row
        genderPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Click the Delete button for the first row
        genderPage.getDeleteButton(0).should(exist);
        genderPage.getDeleteButton(0).should(be(visible));
        genderPage.getDeleteButton(0).click();
        
        // After clicking delete, we should see the delete confirmation screen
        // Wait for HTMX updates to complete and verify the delete confirmation elements appear
        $("#confirm-delete-btn").should(appear, Duration.ofSeconds(3));
        $("#cancel-delete-btn").should(appear, Duration.ofSeconds(3));
        
        // Verify warning message appears
        $(".alert-warning").should(appear);
        
        // Check if we're still on a page with content
        // This verifies the delete button triggered the expected action (showing confirmation)
        $("body").should(exist);
    }
    
    @Test
    @DisplayName("Should create a new Gender record that doesn't clash with existing ones")
    void shouldCreateNewGenderRecordWithoutClash() {
        // Open the gender page
        genderPage.openPage();
        
        // Read all existing gender codes to avoid conflicts
        genderPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Collect existing gender codes from the table
        var existingCodes = new java.util.HashSet<String>();
        var rows = genderPage.getTableRows();
        for (int i = 0; i < rows.size(); i++) {
            var codeCell = rows.get(i).find("td:first-child");
            if (codeCell.exists()) {
                existingCodes.add(codeCell.text().toUpperCase());
            }
        }
        
        // Find a unique code that doesn't clash with existing ones
        String[] candidateCodes = {"Z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "Y"};
        String newCode = null;
        String newDescription = null;
        
        for (String code : candidateCodes) {
            if (!existingCodes.contains(code)) {
                newCode = code;
                newDescription = "Test " + code;
                break;
            }
        }
        
        // Ensure we found a unique code
        assert newCode != null : "Could not find a unique gender code for testing";
        
        // Record initial row count
        int initialRowCount = genderPage.getTableRows().size();
        
        // Click Create button
        genderPage.clickCreate();
        
        // Fill the form with unique values
        genderPage.fillGenderForm(newCode, newDescription);
        
        // Submit the form
        genderPage.clickSave();
        
        // Wait for HTMX to update and verify the new record appears
        genderPage.getTableRows().shouldHave(sizeGreaterThan(initialRowCount));
        
        // Verify the new gender appears in the table
        boolean foundNewGender = false;
        var updatedRows = genderPage.getTableRows();
        for (int i = 0; i < updatedRows.size(); i++) {
            var codeCell = updatedRows.get(i).find("td:first-child");
            if (codeCell.exists() && newCode.equals(codeCell.text())) {
                foundNewGender = true;
                break;
            }
        }
        
        assert foundNewGender : "New gender record was not found in the table";
    }
}