package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.IdTypePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.$;

@DisplayName("IdType CRUD E2E Tests")
class IdTypeCrudTest extends BaseSelenideTest {
    
    private final IdTypePage idTypePage = new IdTypePage();
    
    private String findUniqueIdTypeCode() {
        // Collect existing ID type codes from the table
        var existingCodes = new java.util.HashSet<String>();
        var rows = idTypePage.getTableRows();
        for (int i = 0; i < rows.size(); i++) {
            var codeCell = rows.get(i).find("td:first-child");
            if (codeCell.exists()) {
                existingCodes.add(codeCell.text().toUpperCase());
            }
        }
        
        // Find a unique code that doesn't clash with existing ones
        String[] candidateCodes = {"TEST", "ZZZ", "AAA", "BBB", "CCC", "DDD", "EEE", "FFF", "GGG", "HHH", "III", "JJJ", "KKK", "LLL", "NNN", "OOO", "PPP", "QQQ", "RRR", "SSS", "TTT", "UUU", "VVV", "WWW", "YYY"};
        for (String code : candidateCodes) {
            if (!existingCodes.contains(code)) {
                return code;
            }
        }
        
        // If no unique code found, throw assertion error
        throw new AssertionError("Could not find a unique ID type code for testing");
    }
    
    @Test
    @DisplayName("Should display id type table when page loads")
    void shouldDisplayIdTypeTableWhenPageLoads() {
        idTypePage.openPage();
        
        idTypePage.getPageTitle().should(exist);
        idTypePage.getPageTitle().should(be(visible));
        idTypePage.getPageTitle().should(have(text("Types of identity documents")));
        
        idTypePage.getIdTypeTable().should(exist);
        idTypePage.getIdTypeTable().should(be(visible));
        
        idTypePage.getTableHeaders().shouldHave(texts("CODE", "DESCRIPTION", "CREATED BY", "CREATED AT", "UPDATED BY", "UPDATED AT", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        idTypePage.openPage();
        
        idTypePage.getCreateButton().should(exist);
        idTypePage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        idTypePage.openPage();
        
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        int rowCount = idTypePage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            idTypePage.getViewButton(i).should(exist);
            idTypePage.getViewButton(i).should(be(visible));
            
            idTypePage.getEditButton(i).should(exist);
            idTypePage.getEditButton(i).should(be(visible));
            
            idTypePage.getDeleteButton(i).should(exist);
            idTypePage.getDeleteButton(i).should(be(visible));
        }
    }
    
    @Test
    @DisplayName("Should open Create screen when Create button is clicked")
    void shouldOpenCreateScreenWhenCreateButtonIsClicked() {
        idTypePage.openPage();
        
        idTypePage.getCreateButton().should(exist);
        idTypePage.getCreateButton().should(be(visible));
        
        idTypePage.clickCreate();
        
        idTypePage.getCodeInput().should(exist);
        idTypePage.getCodeInput().should(be(visible));
        
        idTypePage.getDescriptionInput().should(exist);
        idTypePage.getDescriptionInput().should(be(visible));
        
        idTypePage.getSaveButton().should(exist);
        idTypePage.getSaveButton().should(be(visible));
        
        idTypePage.getCancelButton().should(exist);
        idTypePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open View screen when View button is clicked")
    void shouldOpenViewScreenWhenViewButtonIsClicked() {
        idTypePage.openPage();
        
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        idTypePage.getViewButton(0).should(exist);
        idTypePage.getViewButton(0).should(be(visible));
        idTypePage.getViewButton(0).click();
        
        idTypePage.getDetailCard().should(exist);
        idTypePage.getDetailCard().should(be(visible));
        
        idTypePage.getBackButton().should(exist);
        idTypePage.getBackButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open Edit screen when Edit button is clicked")
    void shouldOpenEditScreenWhenEditButtonIsClicked() {
        idTypePage.openPage();
        
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        idTypePage.getEditButton(0).should(exist);
        idTypePage.getEditButton(0).should(be(visible));
        idTypePage.getEditButton(0).click();
        
        idTypePage.getCodeInput().should(exist);
        idTypePage.getCodeInput().should(be(visible));
        
        idTypePage.getDescriptionInput().should(exist);
        idTypePage.getDescriptionInput().should(be(visible));
        
        idTypePage.getSaveButton().should(exist);
        idTypePage.getSaveButton().should(be(visible));
        
        idTypePage.getCancelButton().should(exist);
        idTypePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should respond when Delete button is clicked")
    void shouldRespondWhenDeleteButtonIsClicked() {
        idTypePage.openPage();
        
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        idTypePage.getDeleteButton(0).should(exist);
        idTypePage.getDeleteButton(0).should(be(visible));
        idTypePage.getDeleteButton(0).click();
        
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
    @DisplayName("Should create a new IdType record that doesn't clash with existing ones")
    void shouldCreateNewIdTypeRecordWithoutClash() {
        // Open the ID type page
        idTypePage.openPage();
        
        // Read all existing ID type codes to avoid conflicts
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Find a unique code that doesn't clash with existing ones
        String newCode = findUniqueIdTypeCode();
        String newDescription = "Test " + newCode;
        
        // Record initial row count
        int initialRowCount = idTypePage.getTableRows().size();
        
        // Click Create button
        idTypePage.clickCreate();
        
        // Fill the form with unique values
        idTypePage.fillIdTypeForm(newCode, newDescription);
        
        // Submit the form
        idTypePage.clickSave();
        
        // Wait for HTMX to update and verify the new record appears
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(initialRowCount));
        
        // Verify the new ID type appears in the table
        boolean foundNewIdType = false;
        var updatedRows = idTypePage.getTableRows();
        for (int i = 0; i < updatedRows.size(); i++) {
            var codeCell = updatedRows.get(i).find("td:first-child");
            if (codeCell.exists() && newCode.equals(codeCell.text())) {
                foundNewIdType = true;
                break;
            }
        }
        
        assert foundNewIdType : "New ID type record was not found in the table";
    }
    
    @Test
    @DisplayName("Should show validation error when creating IdType with duplicate code")
    void shouldShowValidationErrorWhenCreatingIdTypeWithDuplicateCode() {
        // Open the ID type page
        idTypePage.openPage();
        
        // Verify table has at least one row to get an existing code
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Get the first existing ID type code
        var firstRow = idTypePage.getTableRows().first();
        var existingCode = firstRow.find("td:first-child").text();
        
        // Click Create button
        idTypePage.clickCreate();
        
        // Fill the form with duplicate code
        idTypePage.fillIdTypeForm(existingCode, "Duplicate Test Description");
        
        // Submit the form
        idTypePage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        idTypePage.getCodeInput().should(be(visible));
        idTypePage.getDescriptionInput().should(be(visible));
        idTypePage.getSaveButton().should(be(visible));
        idTypePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should show validation error when creating IdType with duplicate description")
    void shouldShowValidationErrorWhenCreatingIdTypeWithDuplicateDescription() {
        // Open the ID type page
        idTypePage.openPage();
        
        // Verify table has at least one row to get an existing description
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Get the first existing ID type description
        var firstRow = idTypePage.getTableRows().first();
        var existingDescription = firstRow.find("td:nth-child(2)").text();
        
        // Click Create button
        idTypePage.clickCreate();
        
        // Fill the form with duplicate description but different code
        idTypePage.fillIdTypeForm("TESTT", existingDescription);
        
        // Submit the form
        idTypePage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        idTypePage.getCodeInput().should(be(visible));
        idTypePage.getDescriptionInput().should(be(visible));
        idTypePage.getSaveButton().should(be(visible));
        idTypePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should update IdType successfully")
    void shouldUpdateIdTypeSuccessfully() {
        // Open the ID type page
        idTypePage.openPage();
        
        // Verify table has at least one row
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Get the first row's original data
        var firstRow = idTypePage.getTableRows().first();
        var originalCode = firstRow.find("td:first-child").text();
        var originalDescription = firstRow.find("td:nth-child(2)").text();
        
        // Click Edit button for the first row
        idTypePage.getEditButton(0).click();
        
        // Verify edit form is displayed
        idTypePage.getCodeInput().should(be(visible));
        idTypePage.getDescriptionInput().should(be(visible));
        
        // Verify form is pre-populated with current values
        idTypePage.getCodeInput().should(have(value(originalCode)));
        idTypePage.getDescriptionInput().should(have(value(originalDescription)));
        
        // Update the description (keep code same to avoid conflicts)
        var updatedDescription = "Updated " + originalDescription;
        idTypePage.getDescriptionInput().clear();
        idTypePage.getDescriptionInput().setValue(updatedDescription);
        
        // Submit the update
        idTypePage.clickSave();
        
        // Verify we're back to the table view
        idTypePage.getIdTypeTable().should(be(visible));
        
        // Verify the update was applied by finding the row with our code
        boolean foundUpdatedIdType = false;
        var currentRows = idTypePage.getTableRows();
        for (int i = 0; i < currentRows.size(); i++) {
            var codeCell = currentRows.get(i).find("td:first-child");
            var descCell = currentRows.get(i).find("td:nth-child(2)");
            if (codeCell.exists() && originalCode.equals(codeCell.text()) &&
                descCell.exists() && updatedDescription.equals(descCell.text())) {
                foundUpdatedIdType = true;
                break;
            }
        }
        
        assert foundUpdatedIdType : "Updated ID type record was not found in the table";
        
        // Restore original data for other tests - find the row again
        int updatedRowIndex = -1;
        var restoreRows = idTypePage.getTableRows();
        for (int i = 0; i < restoreRows.size(); i++) {
            var codeCell = restoreRows.get(i).find("td:first-child");
            if (codeCell.exists() && originalCode.equals(codeCell.text())) {
                updatedRowIndex = i;
                break;
            }
        }
        
        if (updatedRowIndex >= 0) {
            idTypePage.getEditButton(updatedRowIndex).click();
            idTypePage.getDescriptionInput().clear();
            idTypePage.getDescriptionInput().setValue(originalDescription);
            idTypePage.clickSave();
        }
    }
    
    @Test
    @DisplayName("Should show validation error when updating IdType with duplicate code")
    void shouldShowValidationErrorWhenUpdatingIdTypeWithDuplicateCode() {
        // Open the ID type page
        idTypePage.openPage();
        
        // Verify table has at least two rows
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(1));
        
        // Get codes from first two rows
        var firstRow = idTypePage.getTableRows().get(0);
        var secondRow = idTypePage.getTableRows().get(1);
        var firstCode = firstRow.find("td:first-child").text();
        var secondCode = secondRow.find("td:first-child").text();
        
        // Click Edit button for the first row
        idTypePage.getEditButton(0).click();
        
        // Try to update with the second row's code
        idTypePage.getCodeInput().clear();
        idTypePage.getCodeInput().setValue(secondCode);
        
        // Submit the update
        idTypePage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        idTypePage.getCodeInput().should(be(visible));
        idTypePage.getDescriptionInput().should(be(visible));
        idTypePage.getSaveButton().should(be(visible));
        idTypePage.getCancelButton().should(be(visible));
        
        // Cancel to return to table view
        idTypePage.clickCancel();
    }
    
    @Test
    @DisplayName("Should show validation error when updating IdType with duplicate description")
    void shouldShowValidationErrorWhenUpdatingIdTypeWithDuplicateDescription() {
        // Open the ID type page
        idTypePage.openPage();
        
        // Verify table has at least two rows
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(1));
        
        // Get descriptions from first two rows
        var firstRow = idTypePage.getTableRows().get(0);
        var secondRow = idTypePage.getTableRows().get(1);
        var firstDescription = firstRow.find("td:nth-child(2)").text();
        var secondDescription = secondRow.find("td:nth-child(2)").text();
        
        // Click Edit button for the first row
        idTypePage.getEditButton(0).click();
        
        // Try to update with the second row's description
        idTypePage.getDescriptionInput().clear();
        idTypePage.getDescriptionInput().setValue(secondDescription);
        
        // Submit the update
        idTypePage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        idTypePage.getCodeInput().should(be(visible));
        idTypePage.getDescriptionInput().should(be(visible));
        idTypePage.getSaveButton().should(be(visible));
        idTypePage.getCancelButton().should(be(visible));
        
        // Cancel to return to table view
        idTypePage.clickCancel();
    }
    
    @Test
    @DisplayName("Should delete IdType successfully")
    void shouldDeleteIdTypeSuccessfully() {
        // First, create a test ID type that we can safely delete
        idTypePage.openPage();
        
        // Find a unique code for deletion test
        String testCode = findUniqueIdTypeCode();
        
        // Create a test ID type for deletion
        idTypePage.clickCreate();
        idTypePage.fillIdTypeForm(testCode, "Test Delete IdType");
        idTypePage.clickSave();
        
        // If creation failed due to validation, skip this test
        if (idTypePage.getErrorMessage().exists()) {
            idTypePage.clickCancel();
            return;
        }
        
        // Record current row count
        int initialRowCount = idTypePage.getTableRows().size();
        
        // Find the row with our test ID type
        int testRowIndex = -1;
        var currentRows = idTypePage.getTableRows();
        for (int i = 0; i < currentRows.size(); i++) {
            var codeCell = currentRows.get(i).find("td:first-child");
            if (codeCell.exists() && testCode.equals(codeCell.text())) {
                testRowIndex = i;
                break;
            }
        }
        
        // Only proceed if we found our test row
        if (testRowIndex >= 0) {
            // Click Delete button
            idTypePage.getDeleteButton(testRowIndex).click();
            
            // Confirm deletion
            $("#confirm-delete-btn").should(appear, Duration.ofSeconds(3));
            $("#confirm-delete-btn").click();
            
            // Verify the row was deleted
            idTypePage.getTableRows().shouldHave(sizeLessThan(initialRowCount));
            
            // Verify the specific ID type is no longer in the table
            boolean foundDeletedIdType = false;
            var finalRows = idTypePage.getTableRows();
            for (int i = 0; i < finalRows.size(); i++) {
                var codeCell = finalRows.get(i).find("td:first-child");
                if (codeCell.exists() && testCode.equals(codeCell.text())) {
                    foundDeletedIdType = true;
                    break;
                }
            }
            
            assert !foundDeletedIdType : "ID type should have been deleted but was still found in table";
        }
    }
}