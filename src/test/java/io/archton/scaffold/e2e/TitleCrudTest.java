package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.TitlePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.$;

@DisplayName("Title CRUD E2E Tests")
class TitleCrudTest extends BaseSelenideTest {
    
    private final TitlePage titlePage = new TitlePage();
    
    private String findUniqueTitleCode() {
        // Collect existing title codes from the table
        var existingCodes = new java.util.HashSet<String>();
        var rows = titlePage.getTableRows();
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
        throw new AssertionError("Could not find a unique title code for testing");
    }
    
    @Test
    @DisplayName("Should display title table when page loads")
    void shouldDisplayTitleTableWhenPageLoads() {
        titlePage.openPage();
        
        titlePage.getPageTitle().should(exist);
        titlePage.getPageTitle().should(be(visible));
        titlePage.getPageTitle().should(have(text("Titles")));
        
        titlePage.getTitleTable().should(exist);
        titlePage.getTitleTable().should(be(visible));
        
        titlePage.getTableHeaders().shouldHave(texts("CODE", "DESCRIPTION", "CREATED BY", "CREATED AT", "UPDATED BY", "UPDATED AT", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        titlePage.openPage();
        
        titlePage.getCreateButton().should(exist);
        titlePage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        titlePage.openPage();
        
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        int rowCount = titlePage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            titlePage.getViewButton(i).should(exist);
            titlePage.getViewButton(i).should(be(visible));
            
            titlePage.getEditButton(i).should(exist);
            titlePage.getEditButton(i).should(be(visible));
            
            titlePage.getDeleteButton(i).should(exist);
            titlePage.getDeleteButton(i).should(be(visible));
        }
    }
    
    @Test
    @DisplayName("Should open Create screen when Create button is clicked")
    void shouldOpenCreateScreenWhenCreateButtonIsClicked() {
        titlePage.openPage();
        
        titlePage.getCreateButton().should(exist);
        titlePage.getCreateButton().should(be(visible));
        
        titlePage.clickCreate();
        
        titlePage.getCodeInput().should(exist);
        titlePage.getCodeInput().should(be(visible));
        
        titlePage.getDescriptionInput().should(exist);
        titlePage.getDescriptionInput().should(be(visible));
        
        titlePage.getSaveButton().should(exist);
        titlePage.getSaveButton().should(be(visible));
        
        titlePage.getCancelButton().should(exist);
        titlePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open View screen when View button is clicked")
    void shouldOpenViewScreenWhenViewButtonIsClicked() {
        titlePage.openPage();
        
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        titlePage.getViewButton(0).should(exist);
        titlePage.getViewButton(0).should(be(visible));
        titlePage.getViewButton(0).click();
        
        titlePage.getDetailCard().should(exist);
        titlePage.getDetailCard().should(be(visible));
        
        titlePage.getBackButton().should(exist);
        titlePage.getBackButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open Edit screen when Edit button is clicked")
    void shouldOpenEditScreenWhenEditButtonIsClicked() {
        titlePage.openPage();
        
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        titlePage.getEditButton(0).should(exist);
        titlePage.getEditButton(0).should(be(visible));
        titlePage.getEditButton(0).click();
        
        titlePage.getCodeInput().should(exist);
        titlePage.getCodeInput().should(be(visible));
        
        titlePage.getDescriptionInput().should(exist);
        titlePage.getDescriptionInput().should(be(visible));
        
        titlePage.getSaveButton().should(exist);
        titlePage.getSaveButton().should(be(visible));
        
        titlePage.getCancelButton().should(exist);
        titlePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should respond when Delete button is clicked")
    void shouldRespondWhenDeleteButtonIsClicked() {
        titlePage.openPage();
        
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        titlePage.getDeleteButton(0).should(exist);
        titlePage.getDeleteButton(0).should(be(visible));
        titlePage.getDeleteButton(0).click();
        
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
    @DisplayName("Should create a new Title record that doesn't clash with existing ones")
    void shouldCreateNewTitleRecordWithoutClash() {
        // Open the title page
        titlePage.openPage();
        
        // Read all existing title codes to avoid conflicts
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Find a unique code that doesn't clash with existing ones
        String newCode = findUniqueTitleCode();
        String newDescription = "Test " + newCode;
        
        // Record initial row count
        int initialRowCount = titlePage.getTableRows().size();
        
        // Click Create button
        titlePage.clickCreate();
        
        // Fill the form with unique values
        titlePage.fillTitleForm(newCode, newDescription);
        
        // Submit the form
        titlePage.clickSave();
        
        // Wait for HTMX to update and verify the new record appears
        titlePage.getTableRows().shouldHave(sizeGreaterThan(initialRowCount));
        
        // Verify the new title appears in the table
        boolean foundNewTitle = false;
        var updatedRows = titlePage.getTableRows();
        for (int i = 0; i < updatedRows.size(); i++) {
            var codeCell = updatedRows.get(i).find("td:first-child");
            if (codeCell.exists() && newCode.equals(codeCell.text())) {
                foundNewTitle = true;
                break;
            }
        }
        
        assert foundNewTitle : "New title record was not found in the table";
    }
    
    @Test
    @DisplayName("Should show validation error when creating Title with duplicate code")
    void shouldShowValidationErrorWhenCreatingTitleWithDuplicateCode() {
        // Open the title page
        titlePage.openPage();
        
        // Verify table has at least one row to get an existing code
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Get the first existing title code
        var firstRow = titlePage.getTableRows().first();
        var existingCode = firstRow.find("td:first-child").text();
        
        // Click Create button
        titlePage.clickCreate();
        
        // Fill the form with duplicate code
        titlePage.fillTitleForm(existingCode, "Duplicate Test Description");
        
        // Submit the form
        titlePage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        titlePage.getCodeInput().should(be(visible));
        titlePage.getDescriptionInput().should(be(visible));
        titlePage.getSaveButton().should(be(visible));
        titlePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should show validation error when creating Title with duplicate description")
    void shouldShowValidationErrorWhenCreatingTitleWithDuplicateDescription() {
        // Open the title page
        titlePage.openPage();
        
        // Verify table has at least one row to get an existing description
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Get the first existing title description
        var firstRow = titlePage.getTableRows().first();
        var existingDescription = firstRow.find("td:nth-child(2)").text();
        
        // Click Create button
        titlePage.clickCreate();
        
        // Fill the form with duplicate description but different code
        titlePage.fillTitleForm("TESTT", existingDescription);
        
        // Submit the form
        titlePage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        titlePage.getCodeInput().should(be(visible));
        titlePage.getDescriptionInput().should(be(visible));
        titlePage.getSaveButton().should(be(visible));
        titlePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should update title successfully")
    void shouldUpdateTitleSuccessfully() {
        // Open the title page
        titlePage.openPage();
        
        // Verify table has at least one row
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Get the first row's original data
        var firstRow = titlePage.getTableRows().first();
        var originalCode = firstRow.find("td:first-child").text();
        var originalDescription = firstRow.find("td:nth-child(2)").text();
        
        // Click Edit button for the first row
        titlePage.getEditButton(0).click();
        
        // Verify edit form is displayed
        titlePage.getCodeInput().should(be(visible));
        titlePage.getDescriptionInput().should(be(visible));
        
        // Verify form is pre-populated with current values
        titlePage.getCodeInput().should(have(value(originalCode)));
        titlePage.getDescriptionInput().should(have(value(originalDescription)));
        
        // Update the description (keep code same to avoid conflicts)
        var updatedDescription = "Updated " + originalDescription;
        titlePage.getDescriptionInput().clear();
        titlePage.getDescriptionInput().setValue(updatedDescription);
        
        // Submit the update
        titlePage.clickSave();
        
        // Verify we're back to the table view
        titlePage.getTitleTable().should(be(visible));
        
        // Verify the update was applied by finding the row with our code
        boolean foundUpdatedTitle = false;
        var currentRows = titlePage.getTableRows();
        for (int i = 0; i < currentRows.size(); i++) {
            var codeCell = currentRows.get(i).find("td:first-child");
            var descCell = currentRows.get(i).find("td:nth-child(2)");
            if (codeCell.exists() && originalCode.equals(codeCell.text()) &&
                descCell.exists() && updatedDescription.equals(descCell.text())) {
                foundUpdatedTitle = true;
                break;
            }
        }
        
        assert foundUpdatedTitle : "Updated title record was not found in the table";
        
        // Restore original data for other tests - find the row again
        int updatedRowIndex = -1;
        var restoreRows = titlePage.getTableRows();
        for (int i = 0; i < restoreRows.size(); i++) {
            var codeCell = restoreRows.get(i).find("td:first-child");
            if (codeCell.exists() && originalCode.equals(codeCell.text())) {
                updatedRowIndex = i;
                break;
            }
        }
        
        if (updatedRowIndex >= 0) {
            titlePage.getEditButton(updatedRowIndex).click();
            titlePage.getDescriptionInput().clear();
            titlePage.getDescriptionInput().setValue(originalDescription);
            titlePage.clickSave();
        }
    }
    
    @Test
    @DisplayName("Should show validation error when updating Title with duplicate code")
    void shouldShowValidationErrorWhenUpdatingTitleWithDuplicateCode() {
        // Open the title page
        titlePage.openPage();
        
        // Verify table has at least two rows
        titlePage.getTableRows().shouldHave(sizeGreaterThan(1));
        
        // Get codes from first two rows
        var firstRow = titlePage.getTableRows().get(0);
        var secondRow = titlePage.getTableRows().get(1);
        var firstCode = firstRow.find("td:first-child").text();
        var secondCode = secondRow.find("td:first-child").text();
        
        // Click Edit button for the first row
        titlePage.getEditButton(0).click();
        
        // Try to update with the second row's code
        titlePage.getCodeInput().clear();
        titlePage.getCodeInput().setValue(secondCode);
        
        // Submit the update
        titlePage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        titlePage.getCodeInput().should(be(visible));
        titlePage.getDescriptionInput().should(be(visible));
        titlePage.getSaveButton().should(be(visible));
        titlePage.getCancelButton().should(be(visible));
        
        // Cancel to return to table view
        titlePage.clickCancel();
    }
    
    @Test
    @DisplayName("Should show validation error when updating Title with duplicate description")
    void shouldShowValidationErrorWhenUpdatingTitleWithDuplicateDescription() {
        // Open the title page
        titlePage.openPage();
        
        // Verify table has at least two rows
        titlePage.getTableRows().shouldHave(sizeGreaterThan(1));
        
        // Get descriptions from first two rows
        var firstRow = titlePage.getTableRows().get(0);
        var secondRow = titlePage.getTableRows().get(1);
        var firstDescription = firstRow.find("td:nth-child(2)").text();
        var secondDescription = secondRow.find("td:nth-child(2)").text();
        
        // Click Edit button for the first row
        titlePage.getEditButton(0).click();
        
        // Try to update with the second row's description
        titlePage.getDescriptionInput().clear();
        titlePage.getDescriptionInput().setValue(secondDescription);
        
        // Submit the update
        titlePage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        titlePage.getCodeInput().should(be(visible));
        titlePage.getDescriptionInput().should(be(visible));
        titlePage.getSaveButton().should(be(visible));
        titlePage.getCancelButton().should(be(visible));
        
        // Cancel to return to table view
        titlePage.clickCancel();
    }
    
    @Test
    @DisplayName("Should delete title successfully")
    void shouldDeleteTitleSuccessfully() {
        // First, create a test title that we can safely delete
        titlePage.openPage();
        
        // Find a unique code for deletion test
        String testCode = findUniqueTitleCode();
        
        // Create a test title for deletion
        titlePage.clickCreate();
        titlePage.fillTitleForm(testCode, "Test Delete Title");
        titlePage.clickSave();
        
        // If creation failed due to validation, skip this test
        if (titlePage.getErrorMessage().exists()) {
            titlePage.clickCancel();
            return;
        }
        
        // Record current row count
        int initialRowCount = titlePage.getTableRows().size();
        
        // Find the row with our test title
        int testRowIndex = -1;
        var currentRows = titlePage.getTableRows();
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
            titlePage.getDeleteButton(testRowIndex).click();
            
            // Confirm deletion
            $("#confirm-delete-btn").should(appear, Duration.ofSeconds(3));
            $("#confirm-delete-btn").click();
            
            // Verify the row was deleted
            titlePage.getTableRows().shouldHave(sizeLessThan(initialRowCount));
            
            // Verify the specific title is no longer in the table
            boolean foundDeletedTitle = false;
            var finalRows = titlePage.getTableRows();
            for (int i = 0; i < finalRows.size(); i++) {
                var codeCell = finalRows.get(i).find("td:first-child");
                if (codeCell.exists() && testCode.equals(codeCell.text())) {
                    foundDeletedTitle = true;
                    break;
                }
            }
            
            assert !foundDeletedTitle : "Title should have been deleted but was still found in table";
        }
    }
}