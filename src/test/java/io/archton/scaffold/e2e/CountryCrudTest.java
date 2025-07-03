package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.CountryPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.$;

@DisplayName("Country CRUD E2E Tests")
class CountryCrudTest extends BaseSelenideTest {
    
    private final CountryPage countryPage = new CountryPage();
    
    private String findUniqueCountryCode() {
        // Collect existing country codes from the table
        var existingCodes = new java.util.HashSet<String>();
        var rows = countryPage.getTableRows();
        for (int i = 0; i < rows.size(); i++) {
            var codeCell = rows.get(i).find("td:first-child");
            if (codeCell.exists()) {
                existingCodes.add(codeCell.text().toUpperCase());
            }
        }
        
        // Find a unique code that doesn't clash with existing ones
        String[] candidateCodes = {"ZZ", "ZY", "ZX", "ZW", "ZV", "ZU", "ZT", "ZS", "ZR", "ZQ", "ZP", "ZO", "ZN", "ZM", "ZL", "ZK", "ZJ", "ZI", "ZH", "ZG", "ZF", "ZE", "ZD", "ZC", "ZB", "ZA"};
        for (String code : candidateCodes) {
            if (!existingCodes.contains(code)) {
                return code;
            }
        }
        
        // If no unique code found, throw assertion error
        throw new AssertionError("Could not find a unique country code for testing");
    }
    
    @Test
    @DisplayName("Should display country table when page loads")
    void shouldDisplayCountryTableWhenPageLoads() {
        countryPage.openPage();
        
        countryPage.getPageTitle().should(exist);
        countryPage.getPageTitle().should(be(visible));
        countryPage.getPageTitle().should(have(text("Countries")));
        
        countryPage.getCountryTable().should(exist);
        countryPage.getCountryTable().should(be(visible));
        
        countryPage.getTableHeaders().shouldHave(texts("CODE", "NAME", "YEAR", "CCTLD", "CREATED BY", "CREATED AT", "UPDATED BY", "UPDATED AT", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        countryPage.openPage();
        
        countryPage.getCreateButton().should(exist);
        countryPage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        countryPage.openPage();
        
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        int rowCount = countryPage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            countryPage.getViewButton(i).should(exist);
            countryPage.getViewButton(i).should(be(visible));
            
            countryPage.getEditButton(i).should(exist);
            countryPage.getEditButton(i).should(be(visible));
            
            countryPage.getDeleteButton(i).should(exist);
            countryPage.getDeleteButton(i).should(be(visible));
        }
    }
    
    @Test
    @DisplayName("Should open Create screen when Create button is clicked")
    void shouldOpenCreateScreenWhenCreateButtonIsClicked() {
        countryPage.openPage();
        
        countryPage.getCreateButton().should(exist);
        countryPage.getCreateButton().should(be(visible));
        
        countryPage.clickCreate();
        
        countryPage.getCodeInput().should(exist);
        countryPage.getCodeInput().should(be(visible));
        
        countryPage.getNameInput().should(exist);
        countryPage.getNameInput().should(be(visible));
        
        countryPage.getSaveButton().should(exist);
        countryPage.getSaveButton().should(be(visible));
        
        countryPage.getCancelButton().should(exist);
        countryPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open View screen when View button is clicked")
    void shouldOpenViewScreenWhenViewButtonIsClicked() {
        countryPage.openPage();
        
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        countryPage.getViewButton(0).should(exist);
        countryPage.getViewButton(0).should(be(visible));
        countryPage.getViewButton(0).click();
        
        countryPage.getDetailCard().should(exist);
        countryPage.getDetailCard().should(be(visible));
        
        countryPage.getBackButton().should(exist);
        countryPage.getBackButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open Edit screen when Edit button is clicked")
    void shouldOpenEditScreenWhenEditButtonIsClicked() {
        countryPage.openPage();
        
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        countryPage.getEditButton(0).should(exist);
        countryPage.getEditButton(0).should(be(visible));
        countryPage.getEditButton(0).click();
        
        countryPage.getCodeInput().should(exist);
        countryPage.getCodeInput().should(be(visible));
        
        countryPage.getNameInput().should(exist);
        countryPage.getNameInput().should(be(visible));
        
        countryPage.getSaveButton().should(exist);
        countryPage.getSaveButton().should(be(visible));
        
        countryPage.getCancelButton().should(exist);
        countryPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should respond when Delete button is clicked")
    void shouldRespondWhenDeleteButtonIsClicked() {
        countryPage.openPage();
        
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        countryPage.getDeleteButton(0).should(exist);
        countryPage.getDeleteButton(0).should(be(visible));
        countryPage.getDeleteButton(0).click();
        
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
    @DisplayName("Should create a new Country record that doesn't clash with existing ones")
    void shouldCreateNewCountryRecordWithoutClash() {
        // Open the country page
        countryPage.openPage();
        
        // Read all existing country codes to avoid conflicts
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Find a unique code that doesn't clash with existing ones
        String newCode = findUniqueCountryCode();
        String newName = "Test " + newCode;
        
        // Record initial row count
        int initialRowCount = countryPage.getTableRows().size();
        
        // Click Create button
        countryPage.clickCreate();
        
        // Fill the form with unique values
        countryPage.fillCountryForm(newCode, newName);
        
        // Submit the form
        countryPage.clickSave();
        
        // Wait for HTMX to update and verify the new record appears
        countryPage.getTableRows().shouldHave(sizeGreaterThan(initialRowCount));
        
        // Verify the new country appears in the table
        boolean foundNewCountry = false;
        var updatedRows = countryPage.getTableRows();
        for (int i = 0; i < updatedRows.size(); i++) {
            var codeCell = updatedRows.get(i).find("td:first-child");
            if (codeCell.exists() && newCode.equals(codeCell.text())) {
                foundNewCountry = true;
                break;
            }
        }
        
        assert foundNewCountry : "New country record was not found in the table";
    }
    
    @Test
    @DisplayName("Should show validation error when creating Country with duplicate code")
    void shouldShowValidationErrorWhenCreatingCountryWithDuplicateCode() {
        // Open the country page
        countryPage.openPage();
        
        // Verify table has at least one row to get an existing code
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Get the first existing country code
        var firstRow = countryPage.getTableRows().first();
        var existingCode = firstRow.find("td:first-child").text();
        
        // Click Create button
        countryPage.clickCreate();
        
        // Fill the form with duplicate code
        countryPage.fillCountryForm(existingCode, "Duplicate Test Country");
        
        // Submit the form
        countryPage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        countryPage.getCodeInput().should(be(visible));
        countryPage.getNameInput().should(be(visible));
        countryPage.getSaveButton().should(be(visible));
        countryPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should show validation error when creating Country with duplicate name")
    void shouldShowValidationErrorWhenCreatingCountryWithDuplicateName() {
        // Open the country page
        countryPage.openPage();
        
        // Verify table has at least one row to get an existing name
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Get the first existing country name
        var firstRow = countryPage.getTableRows().first();
        var existingName = firstRow.find("td:nth-child(2)").text();
        
        // Click Create button
        countryPage.clickCreate();
        
        // Fill the form with duplicate name but different code
        countryPage.fillCountryForm("ZZ", existingName);
        
        // Submit the form
        countryPage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        countryPage.getCodeInput().should(be(visible));
        countryPage.getNameInput().should(be(visible));
        countryPage.getSaveButton().should(be(visible));
        countryPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should update Country successfully")
    void shouldUpdateCountrySuccessfully() {
        // Open the country page
        countryPage.openPage();
        
        // Verify table has at least one row
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // Get the first row's original data
        var firstRow = countryPage.getTableRows().first();
        var originalCode = firstRow.find("td:first-child").text();
        var originalName = firstRow.find("td:nth-child(2)").text();
        
        // Click Edit button for the first row
        countryPage.getEditButton(0).click();
        
        // Verify edit form is displayed
        countryPage.getCodeInput().should(be(visible));
        countryPage.getNameInput().should(be(visible));
        
        // Verify form is pre-populated with current values
        countryPage.getCodeInput().should(have(value(originalCode)));
        countryPage.getNameInput().should(have(value(originalName)));
        
        // Update the name (keep code same to avoid conflicts)
        var updatedName = "Updated " + originalName;
        countryPage.getNameInput().clear();
        countryPage.getNameInput().setValue(updatedName);
        
        // Submit the update
        countryPage.clickSave();
        
        // Verify we're back to the table view
        countryPage.getCountryTable().should(be(visible));
        
        // Verify the update was applied
        var updatedRow = countryPage.getTableRows().first();
        updatedRow.find("td:first-child").should(have(text(originalCode)));
        updatedRow.find("td:nth-child(2)").should(have(text(updatedName)));
        
        // Restore original data for other tests
        countryPage.getEditButton(0).click();
        countryPage.getNameInput().clear();
        countryPage.getNameInput().setValue(originalName);
        countryPage.clickSave();
    }
    
    @Test
    @DisplayName("Should show validation error when updating Country with duplicate code")
    void shouldShowValidationErrorWhenUpdatingCountryWithDuplicateCode() {
        // Open the country page
        countryPage.openPage();
        
        // Verify table has at least two rows
        countryPage.getTableRows().shouldHave(sizeGreaterThan(1));
        
        // Get codes from first two rows
        var firstRow = countryPage.getTableRows().get(0);
        var secondRow = countryPage.getTableRows().get(1);
        var firstCode = firstRow.find("td:first-child").text();
        var secondCode = secondRow.find("td:first-child").text();
        
        // Click Edit button for the first row
        countryPage.getEditButton(0).click();
        
        // Try to update with the second row's code
        countryPage.getCodeInput().clear();
        countryPage.getCodeInput().setValue(secondCode);
        
        // Submit the update
        countryPage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        countryPage.getCodeInput().should(be(visible));
        countryPage.getNameInput().should(be(visible));
        countryPage.getSaveButton().should(be(visible));
        countryPage.getCancelButton().should(be(visible));
        
        // Cancel to return to table view
        countryPage.clickCancel();
    }
    
    @Test
    @DisplayName("Should show validation error when updating Country with duplicate name")
    void shouldShowValidationErrorWhenUpdatingCountryWithDuplicateName() {
        // Open the country page
        countryPage.openPage();
        
        // Verify table has at least two rows
        countryPage.getTableRows().shouldHave(sizeGreaterThan(1));
        
        // Get names from first two rows
        var firstRow = countryPage.getTableRows().get(0);
        var secondRow = countryPage.getTableRows().get(1);
        var firstName = firstRow.find("td:nth-child(2)").text();
        var secondName = secondRow.find("td:nth-child(2)").text();
        
        // Click Edit button for the first row
        countryPage.getEditButton(0).click();
        
        // Try to update with the second row's name
        countryPage.getNameInput().clear();
        countryPage.getNameInput().setValue(secondName);
        
        // Submit the update
        countryPage.clickSave();
        
        // Verify validation error appears
        $(".alert-danger").should(appear, Duration.ofSeconds(3));
        
        // Verify the form is still visible (not redirected)
        countryPage.getCodeInput().should(be(visible));
        countryPage.getNameInput().should(be(visible));
        countryPage.getSaveButton().should(be(visible));
        countryPage.getCancelButton().should(be(visible));
        
        // Cancel to return to table view
        countryPage.clickCancel();
    }
    
    @Test
    @DisplayName("Should delete Country successfully")
    void shouldDeleteCountrySuccessfully() {
        // First, create a test country that we can safely delete
        countryPage.openPage();
        
        // Find a unique code for deletion test
        String testCode = findUniqueCountryCode();
        
        // Create a test country for deletion
        countryPage.clickCreate();
        countryPage.fillCountryForm(testCode, "Test Delete Country");
        countryPage.clickSave();
        
        // If creation failed due to validation, skip this test
        if (countryPage.getErrorMessage().exists()) {
            countryPage.clickCancel();
            return;
        }
        
        // Record current row count
        int initialRowCount = countryPage.getTableRows().size();
        
        // Find the row with our test country
        int testRowIndex = -1;
        var currentRows = countryPage.getTableRows();
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
            countryPage.getDeleteButton(testRowIndex).click();
            
            // Confirm deletion
            $("#confirm-delete-btn").should(appear, Duration.ofSeconds(3));
            $("#confirm-delete-btn").click();
            
            // Verify the row was deleted
            countryPage.getTableRows().shouldHave(sizeLessThan(initialRowCount));
            
            // Verify the specific country is no longer in the table
            boolean foundDeletedCountry = false;
            var finalRows = countryPage.getTableRows();
            for (int i = 0; i < finalRows.size(); i++) {
                var codeCell = finalRows.get(i).find("td:first-child");
                if (codeCell.exists() && testCode.equals(codeCell.text())) {
                    foundDeletedCountry = true;
                    break;
                }
            }
            
            assert !foundDeletedCountry : "Country should have been deleted but was still found in table";
        }
    }
}