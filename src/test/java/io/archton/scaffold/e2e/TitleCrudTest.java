package io.archton.scaffold.e2e;

import com.codeborne.selenide.SelenideElement;
import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.TitlePage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Title CRUD E2E Tests")
class TitleCrudTest extends BaseSelenideTest {

    private final TitlePage titlePage = new TitlePage();

    @BeforeEach
    void setUp() {
        // Navigate to titles page before each test
        titlePage.openPage();

        // Wait for table to load
        titlePage.getTitleTable().should(exist);
    }

    @Test
    @DisplayName("Should display title list table with correct headers")
    void shouldDisplayTitleListTable() {
        // Verify table exists and is visible
        titlePage.getTitleTable().should(exist);
        titlePage.getTitleTable().should(be(visible));
        assertTrue(titlePage.isTableVisible(), "Title table should be visible");

        // Verify table headers
        var headers = titlePage.getTableHeaders();
        headers.shouldHave(size(7));
        headers.get(0).should(have(text("Code")));
        headers.get(1).should(have(text("Description")));
        headers.get(2).should(have(text("Created By")));
        headers.get(3).should(have(text("Created At")));
        headers.get(4).should(have(text("Updated By")));
        headers.get(5).should(have(text("Updated At")));
        headers.get(6).should(have(text("Actions")));

        // Verify Create button exists
        $("#create-new-btn").should(exist);
        $("#create-new-btn").should(be(visible));
        $("#create-new-btn").should(have(text("Create")));
    }

    @Test
    @DisplayName("Should create a new title successfully")
    void shouldCreateNewTitle() {
        // Generate unique code and description for the test
        String uniqueCode = findUniqueCode();
        String description = "Test Title " + System.currentTimeMillis();

        // Create a new title using the extracted method
        createTitle(uniqueCode, description);

        // Verify the new title appears in the table
        assertTrue(titlePage.hasTitleWithCode(uniqueCode),
                "Newly created title with code " + uniqueCode + " should exist");

        // Get the row and verify the description
        SelenideElement newRow = titlePage.getRowByCode(uniqueCode);
        assertNotNull(newRow, "New title row should not be null");

        // Verify description
        newRow.$$("td").get(1).shouldHave(text(description));


        // Verify creation details
        newRow.$$("td").get(2).shouldHave(text("system")); // Created by
        newRow.$$("td").get(3).shouldNotBe(empty); // Created at
    }

    @Test
    @DisplayName("Should view title details")
    void shouldViewTitleDetails() {
        // Find first row and get its ID
        var firstRow = titlePage.getTableRows().first();
        firstRow.should(exist);

        // Extract title ID from the view button
        var viewButton = firstRow.$("button[id^='view-btn-']");
        var titleId = viewButton.getAttribute("id").replace("view-btn-", "");

        // Click View button
        titlePage.clickViewButton(Long.parseLong(titleId));

        // Verify view form loads
        titlePage.getContentArea().should(exist);
        titlePage.getCodeValue().should(exist);
        titlePage.getCodeValue().should(be(visible));

        // Verify Back button exists
        $("button:has(.bi-arrow-left)").should(exist);
        $("button:has(.bi-arrow-left)").should(have(text("Back")));

        // Click Back button
        titlePage.clickBackButton();

        // Verify return to list
        titlePage.getTitleTable().should(exist);
        titlePage.getPageTitle().should(have(text("Titles")));
    }

    @Test
    @DisplayName("Should edit an existing title")
    void shouldEditExistingTitle() {
        // Find first row and get its ID
        var firstRow = titlePage.getTableRows().first();
        firstRow.should(exist);

        // Extract title ID and current code from the row
        var editButton = firstRow.$("button[id^='edit-btn-']");
        var titleId = Long.parseLong(editButton.getAttribute("id").replace("edit-btn-", ""));
        var currentCode = firstRow.$("td.fw-bold").getText();
        var currentDescription = firstRow.$$("td").get(1).getText();

        // Generate a unique code and description
        String newCode = findUniqueCode();
        String newDescription = "Updated " + System.currentTimeMillis();

        // Click Edit button
        titlePage.clickEditButton(titleId);

        // Verify edit form loads
        titlePage.getCodeInput().should(exist);
        titlePage.getDescriptionInput().should(exist);

        // Make changes
        titlePage.enterCode(newCode)
                .enterDescription(newDescription);

        // Submit changes
        titlePage.clickSubmitEditButton();

        // Verify return to list
        titlePage.getTitleTable().should(exist);

        // Find the updated row by code and verify
        assertTrue(titlePage.hasTitleWithCode(newCode), "Updated title with code " + newCode + " should exist");

        SelenideElement updatedRow = titlePage.getRowByCode(newCode);
        assertNotNull(updatedRow, "Updated row should not be null");

        // Verify description was updated
        updatedRow.$$("td").get(1).shouldHave(text(newDescription));

    }

    @Test
    @DisplayName("Should delete an existing title")
    void shouldDeleteExistingTitle() {
        // Create a new title to ensure we have something to delete
        String uniqueCode = findUniqueCode();
        String description = "Delete Test " + System.currentTimeMillis();

        // Create the title using the extracted method
        createTitle(uniqueCode, description);

        // Verify the new title appears in the table
        assertTrue(titlePage.hasTitleWithCode(uniqueCode),
                "Newly created title with code " + uniqueCode + " should exist before deletion");

        // Get the row and extract the ID for deletion
        SelenideElement targetRow = titlePage.getRowByCode(uniqueCode);
        assertNotNull(targetRow, "Target row should not be null");

        // Extract title ID from the delete button
        SelenideElement deleteButton = targetRow.$("button[id^='delete-btn-']");
        Long titleId = Long.parseLong(deleteButton.getAttribute("id").replace("delete-btn-", ""));

        // Click Delete button
        titlePage.clickDeleteButton(titleId);

        // Verify delete confirmation page loads
        titlePage.getContentArea().should(exist);
        $(".card-header.bg-danger").should(exist);
        $(".card-header.bg-danger").should(have(text("Delete Title")));
        $(".alert.alert-warning").should(exist);
        $("#confirm-delete-btn").should(exist);
        $("#cancel-delete-btn").should(exist);

        // Verify the correct code and description are shown
        $(".badge.bg-primary.fs-6").should(have(text(uniqueCode)));

        // Click Confirm Delete button
        titlePage.clickConfirmDeleteButton();

        // Verify return to list
        titlePage.getTitleTable().should(exist);

        // Verify the title is no longer in the table
        assertFalse(titlePage.hasTitleWithCode(uniqueCode),
                "Title with code " + uniqueCode + " should not exist after deletion");
    }

    /**
     * Helper method to create a new title with the given attributes
     *
     * @param code The title code (uppercase characters)
     * @param description The title description
     */
    private void createTitle(String code, String description) {
        // Click Create button
        titlePage.clickCreateButton();

        // Verify create form loads
        titlePage.getCodeInput().should(exist);
        titlePage.getDescriptionInput().should(exist);

        // Fill in the form
        titlePage.enterCode(code)
                .enterDescription(description);

        // Submit the form
        titlePage.clickSubmitCreateButton();

        // Verify return to list
        titlePage.getTitleTable().should(exist);
    }

    /**
     * Helper method to find a unique code not in the table
     *
     * @return A unique code
     */
    private String findUniqueCode() {
        // Generate a code from available uppercase letters
        String availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // Try each letter combination until we find one that's not in use
        for (int i = 0; i < availableChars.length(); i++) {
            String candidateCode = String.valueOf(availableChars.charAt(i));
            if (!titlePage.hasTitleWithCode(candidateCode)) {
                return candidateCode;
            }
        }

        // Try two-character combinations
        for (int i = 0; i < availableChars.length(); i++) {
            for (int j = 0; j < availableChars.length(); j++) {
                String candidateCode = String.valueOf(availableChars.charAt(i)) + 
                                       String.valueOf(availableChars.charAt(j));
                if (!titlePage.hasTitleWithCode(candidateCode)) {
                    return candidateCode;
                }
            }
        }

        // If all combinations are in use (unlikely), use timestamp-based code
        return "TT" + String.valueOf(System.currentTimeMillis()).substring(8);
    }
}